from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.dependencies import get_current_user, get_db
from app.models.user import User
from app.schemas.gift import GiftCreate, GiftRead, GiftShort, GiftUpdate
from app.services.gift_service import GiftService


router = APIRouter(prefix="/gifts", tags=["Gifts"])


@router.post(
    "/",
    response_model=GiftRead,
    status_code=status.HTTP_201_CREATED,
    description="Создать новый подарок в вишлисте"
)
def create_gift(
    data: GiftCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = GiftService(db)
    try:
        gift = service.create_for_user(current_user.user_id, data)
        return gift
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.get(
    "/wishlist/{wishlist_id}",
    response_model=List[GiftShort],
    description="Получить все подарки вишлиста"
)
def list_gifts_in_wishlist(
    wishlist_id: int,
    offset: int = 0,
    limit: int = 50,
    status: Optional[str] = None,
    search: Optional[str] = None,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = GiftService(db)
    try:
        gifts = service.list_for_wishlist(
            owner_id=current_user.user_id,
            wishlist_id=wishlist_id,
            offset=offset,
            limit=limit,
            status=status,
            search=search,
        )
        return gifts
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.get(
    "/{gift_id}",
    response_model=GiftRead,
    description="Получить подарок по ID"
)
def get_gift_by_id(
    gift_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = GiftService(db)
    try:
        gift = service.get_for_owner(gift_id, current_user.user_id)
        return gift
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.patch(
    "/{gift_id}",
    response_model=GiftRead,
    description="Обновить подарок"
)
def update_gift(
    gift_id: int,
    data: GiftUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = GiftService(db)
    try:
        updated = service.update_for_owner(gift_id, current_user.user_id, data)
        return updated
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.delete(
    "/{gift_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    description="Удалить подарок"
)
def delete_gift(
    gift_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = GiftService(db)
    try:
        service.delete_for_owner(gift_id, current_user.user_id)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.patch(
    "/{gift_id}/status",
    response_model=GiftRead,
    description="Изменить статус подарка вручную"
)
def change_gift_status(
    gift_id: int,
    new_status: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = GiftService(db)
    try:
        gift = service.change_status_for_owner(
            gift_id=gift_id,
            owner_id=current_user.user_id,
            new_status=new_status,
        )
        return gift
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))
