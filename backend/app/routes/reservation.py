from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db
from app.schemas.reservation import ReservationCreate, ReservationRead
from app.services.reservation_service import ReservationService

router = APIRouter(prefix="/reservations", tags=["reservations"])

@router.post("/", response_model=ReservationRead, status_code=status.HTTP_201_CREATED)
def reserve_gift(user_id: int, gift_id: int, db: Session = Depends(get_db)):
    """Reserve a gift"""
    service = ReservationService(db)
    try:
        reservation = service.reserve_gift(user_id, gift_id)
        return reservation
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=400 if isinstance(e, ValueError) else 403, detail=str(e))

@router.get("/{reservation_id}")
def get_reservation(reservation_id: int, user_id: int, db: Session = Depends(get_db)):
    """Get a reservation by ID for the owner"""
    service = ReservationService(db)
    try:
        reservation = service.get_for_user(reservation_id, user_id)
        return reservation
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.get("/user/{user_id}")
def list_user_reservations(
    user_id: int,
    offset: int = 0,
    limit: int = 50,
    only_active: bool = False,
    db: Session = Depends(get_db)
):
    """List reservations for a user"""
    service = ReservationService(db)
    try:
        reservations = service.list_for_user(
            user_id=user_id,
            offset=offset,
            limit=limit,
            only_active=only_active
        )
        return reservations
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.get("/gift/{gift_id}")
def list_gift_reservations(
    gift_id: int,
    owner_id: int,
    offset: int = 0,
    limit: int = 50,
    only_active: bool = False,
    db: Session = Depends(get_db)
):
    """List reservations for a gift"""
    service = ReservationService(db)
    try:
        reservations = service.list_for_gift(
            gift_id=gift_id,
            owner_id=owner_id,
            offset=offset,
            limit=limit,
            only_active=only_active
        )
        return reservations
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.post("/{reservation_id}/cancel")
def cancel_reservation(reservation_id: int, user_id: int, db: Session = Depends(get_db)):
    """Cancel a reservation"""
    service = ReservationService(db)
    try:
        reservation = service.cancel_for_user(reservation_id, user_id)
        return reservation
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=404 if isinstance(e, ValueError) else 403, detail=str(e))

@router.delete("/{reservation_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_reservation(reservation_id: int, db: Session = Depends(get_db)):
    """Delete a reservation (admin only)"""
    service = ReservationService(db)
    try:
        service.admin_delete(reservation_id)
        return
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))