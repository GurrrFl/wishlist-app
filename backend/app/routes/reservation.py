from typing import List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.dependencies import get_current_user, get_db
from app.models.user import User
from app.schemas.reservation import ReservationRead
from app.services.reservation_service import ReservationService


router = APIRouter(prefix="/reservations", tags=["Reservations"])


@router.post(
    "/gift/{gift_id}",
    response_model=ReservationRead,
    status_code=status.HTTP_201_CREATED,
    description="Зарезервировать подарок"
)
def reserve_gift(
    gift_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = ReservationService(db)
    try:
        reservation = service.reserve_gift(
            user_id=current_user.user_id,
            gift_id=gift_id,
        )
        return reservation
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.get(
    "/my",
    response_model=List[ReservationRead],
    description="Получить все резервации текущего пользователя"
)
def get_my_reservations(
    offset: int = 0,
    limit: int = 50,
    only_active: bool = False,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = ReservationService(db)
    reservations = service.list_for_user(
        user_id=current_user.user_id,
        offset=offset,
        limit=limit,
        only_active=only_active,
    )
    return reservations


@router.get(
    "/{reservation_id}",
    response_model=ReservationRead,
    description="Получить резервацию по ID"
)
def get_reservation_by_id(
    reservation_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = ReservationService(db)
    try:
        reservation = service.get_for_user(reservation_id, current_user.user_id)
        return reservation
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.get(
    "/gift/{gift_id}",
    response_model=List[ReservationRead],
    description="Получить все резервации конкретного подарка (для владельца вишлиста)"
)
def get_gift_reservations(
    gift_id: int,
    offset: int = 0,
    limit: int = 50,
    only_active: bool = False,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = ReservationService(db)
    try:
        reservations = service.list_for_gift(
            gift_id=gift_id,
            owner_id=current_user.user_id,
            offset=offset,
            limit=limit,
            only_active=only_active,
        )
        return reservations
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.post(
    "/{reservation_id}/cancel",
    response_model=ReservationRead,
    description="Отменить резервацию"
)
def cancel_reservation(
    reservation_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = ReservationService(db)
    try:
        cancelled = service.cancel_for_user(
            reservation_id=reservation_id,
            user_id=current_user.user_id,
        )
        return cancelled
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))


@router.delete(
    "/{reservation_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    description="Полностью удалить резервацию (админ-операция)"
)
def delete_reservation(
    reservation_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = ReservationService(db)
    try:
        service.admin_delete(reservation_id)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
