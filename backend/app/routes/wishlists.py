from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db
from app.schemas.wishlist import WishlistCreate, WishlistRead, WishlistUpdate
from app.services.wishlist_service import WishlistService

router = APIRouter(prefix="/wishlists", tags=["wishlists"])

@router.post("/", response_model=WishlistRead, status_code=status.HTTP_201_CREATED)
def create_wishlist(user_id: int, wishlist_data: WishlistCreate, db: Session = Depends(get_db)):
    """Create a new wishlist for a user"""
    service = WishlistService(db)
    try:
        wishlist = service.create_for_user(user_id, wishlist_data)
        return wishlist
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.get("/{wishlist_id}")
def get_wishlist(wishlist_id: int, user_id: int, db: Session = Depends(get_db)):
    """Get a wishlist by ID for the owner"""
    service = WishlistService(db)
    try:
        wishlist = service.get_for_owner(wishlist_id, user_id)
        return wishlist
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.get("/link/{unique_link}")
def get_wishlist_by_link(unique_link: str, db: Session = Depends(get_db)):
    """Get a public wishlist by unique link"""
    service = WishlistService(db)
    wishlist = service.get_public_by_link(unique_link)
    if wishlist is None:
        raise HTTPException(status_code=404, detail="Wishlist not found or is private")
    return wishlist

@router.get("/user/{user_id}")
def list_user_wishlists(
    user_id: int,
    offset: int = 0,
    limit: int = 50,
    include_private: bool = True,
    search: str = None,
    db: Session = Depends(get_db)
):
    """List wishlists for a user"""
    service = WishlistService(db)
    wishlists = service.list_for_user(
        user_id=user_id,
        offset=offset,
        limit=limit,
        include_private=include_private,
        search=search
    )
    return wishlists

@router.put("/{wishlist_id}")
def update_wishlist(
    wishlist_id: int,
    user_id: int,
    wishlist_data: WishlistUpdate,
    db: Session = Depends(get_db)
):
    """Update a wishlist"""
    service = WishlistService(db)
    try:
        wishlist = service.update_for_user(wishlist_id, user_id, wishlist_data)
        return wishlist
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.delete("/{wishlist_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_wishlist(wishlist_id: int, user_id: int, db: Session = Depends(get_db)):
    """Delete a wishlist"""
    service = WishlistService(db)
    try:
        service.delete_for_user(wishlist_id, user_id)
        return
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.post("/{wishlist_id}/regenerate-link")
def regenerate_link(wishlist_id: int, user_id: int, db: Session = Depends(get_db)):
    """Regenerate unique link for a wishlist"""
    service = WishlistService(db)
    try:
        wishlist = service.regenerate_unique_link(wishlist_id, user_id)
        return wishlist
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.post("/{wishlist_id}/clear-link")
def clear_link(wishlist_id: int, user_id: int, db: Session = Depends(get_db)):
    """Clear unique link for a wishlist"""
    service = WishlistService(db)
    try:
        wishlist = service.clear_unique_link(wishlist_id, user_id)
        return wishlist
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))