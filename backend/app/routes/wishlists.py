from typing import List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.dependencies import get_current_user, get_db
from app.models.user import User
from app.schemas.wishlist import WishlistCreate, WishlistRead, WishlistShort, WishlistUpdate
from app.services.wishlist_service import WishlistService


router = APIRouter(prefix="/wishlists", tags=["Wishlists"])


@router.post(
    "/",
    response_model=WishlistRead,
    status_code=status.HTTP_201_CREATED,
    description="Создать новый вишлист"
)
def create_wishlist(
    data: WishlistCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = WishlistService(db)
    wishlist = service.create_for_user(
        user_id=current_user.user_id,
        data=data,
        generate_link_if_missing=True,
    )
    return wishlist


@router.get(
    "/my",
    response_model=List[WishlistShort],
    description="Получить все вишлисты текущего пользователя"
)
def get_my_wishlists(
    offset: int = 0,
    limit: int = 50,
    include_private: bool = True,
    search: str = None,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = WishlistService(db)
    wishlists = service.list_for_user(
        user_id=current_user.user_id,
        offset=offset,
        limit=limit,
        include_private=include_private,
        search=search,
    )
    return wishlists


@router.get(
    "/link/{unique_link}",
    response_model=WishlistRead,
    description="Получить публичный вишлист по уникальной ссылке"
)
def get_wishlist_by_link(
    unique_link: str,
    db: Session = Depends(get_db),
):
    service = WishlistService(db)
    wishlist = service.get_public_by_link(unique_link)
    if wishlist is None:
        raise HTTPException(
            status_code=404,
            detail=f"Public wishlist with link '{unique_link}' not found"
        )
    return wishlist


@router.get(
    "/{wishlist_id}",
    response_model=WishlistRead,
    description="Получить вишлист по ID (только владелец)"
)
def get_wishlist_by_id(
    wishlist_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = WishlistService(db)
    try:
        wishlist = service.get_for_owner(wishlist_id, current_user.user_id)
        return wishlist
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.patch(
    "/{wishlist_id}",
    response_model=WishlistRead,
    description="Обновить вишлист"
)
def update_wishlist(
    wishlist_id: int,
    data: WishlistUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = WishlistService(db)
    try:
        updated = service.update_for_user(
            wishlist_id=wishlist_id,
            owner_id=current_user.user_id,
            data=data,
        )
        return updated
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.delete(
    "/{wishlist_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    description="Удалить вишлист"
)
def delete_wishlist(
    wishlist_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = WishlistService(db)
    try:
        service.delete_for_user(wishlist_id, current_user.user_id)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.post(
    "/{wishlist_id}/regenerate-link",
    response_model=WishlistRead,
    description="Сгенерировать новую уникальную ссылку для вишлиста"
)
def regenerate_wishlist_link(
    wishlist_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = WishlistService(db)
    try:
        wishlist = service.regenerate_unique_link(wishlist_id, current_user.user_id)
        return wishlist
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.delete(
    "/{wishlist_id}/link",
    response_model=WishlistRead,
    description="Удалить уникальную ссылку вишлиста"
)
def clear_wishlist_link(
    wishlist_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = WishlistService(db)
    try:
        wishlist = service.clear_unique_link(wishlist_id, current_user.user_id)
        return wishlist
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))
