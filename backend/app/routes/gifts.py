from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db
from app.schemas.gift import GiftCreate, GiftRead, GiftUpdate
from app.services.gift_service import GiftService

router = APIRouter(prefix="/gifts", tags=["gifts"])

@router.post("/", response_model=GiftRead, status_code=status.HTTP_201_CREATED)
def create_gift(owner_id: int, gift_data: GiftCreate, db: Session = Depends(get_db)):
    """Create a new gift in a wishlist"""
    service = GiftService(db)
    try:
        gift = service.create_for_user(owner_id, gift_data)
        return gift
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=400 if isinstance(e, ValueError) else 403, detail=str(e))

@router.get("/{gift_id}")
def get_gift(gift_id: int, owner_id: int, db: Session = Depends(get_db)):
    """Get a gift by ID for the owner"""
    service = GiftService(db)
    try:
        gift = service.get_for_owner(gift_id, owner_id)
        return gift
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.get("/wishlist/{wishlist_id}")
def list_wishlist_gifts(
    owner_id: int,
    wishlist_id: int,
    offset: int = 0,
    limit: int = 50,
    status: str = None,
    search: str = None,
    db: Session = Depends(get_db)
):
    """List gifts in a wishlist"""
    service = GiftService(db)
    try:
        gifts = service.list_for_wishlist(
            owner_id=owner_id,
            wishlist_id=wishlist_id,
            offset=offset,
            limit=limit,
            status=status,
            search=search
        )
        return gifts
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.put("/{gift_id}")
def update_gift(
    gift_id: int,
    owner_id: int,
    gift_data: GiftUpdate,
    db: Session = Depends(get_db)
):
    """Update a gift"""
    service = GiftService(db)
    try:
        gift = service.update_for_owner(gift_id, owner_id, gift_data)
        return gift
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.delete("/{gift_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_gift(gift_id: int, owner_id: int, db: Session = Depends(get_db)):
    """Delete a gift"""
    service = GiftService(db)
    try:
        service.delete_for_owner(gift_id, owner_id)
        return
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.post("/{gift_id}/status")
def change_gift_status(
    gift_id: int,
    owner_id: int,
    new_status: str,
    db: Session = Depends(get_db)
):
    """Change gift status"""
    service = GiftService(db)
    try:
        gift = service.change_status_for_owner(gift_id, owner_id, new_status)
        return gift
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))