from datetime import datetime
from typing import List, Optional

from sqlalchemy.orm import Session

from app.models.reservation import Reservation
from app.schemas.reservation import ReservationCreate, ReservationUpdate


class ReservationRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_by_id(self, reservation_id: int) -> Optional[Reservation]:
        return self.db.query(Reservation).filter(Reservation.reservation_id == reservation_id).first()

    def get_by_user_and_gift(self, user_id: int, gift_id: int) -> Optional[Reservation]:
        """Получить резервацию (любую: активную или отменённую)"""
        return self.db.query(Reservation).filter(
            Reservation.user_id == user_id,
            Reservation.gift_id == gift_id,
        ).first()

    def list_by_user(
        self,
        user_id: int,
        offset: int = 0,
        limit: int = 50,
        only_active: bool = False,
    ) -> List[Reservation]:
        query = self.db.query(Reservation).filter(Reservation.user_id == user_id)
        if only_active:
            query = query.filter(Reservation.cancelled_at.is_(None))  # Только активные
        return query.offset(offset).limit(limit).all()

    def list_by_gift(
        self,
        gift_id: int,
        offset: int = 0,
        limit: int = 50,
        only_active: bool = False,
    ) -> List[Reservation]:
        query = self.db.query(Reservation).filter(Reservation.gift_id == gift_id)
        if only_active:
            query = query.filter(Reservation.cancelled_at.is_(None))  # Только активные
        return query.offset(offset).limit(limit).all()

    def create(self, data: ReservationCreate) -> Reservation:
        """Создать новую активную резервацию (cancelled_at = NULL)"""
        reservation = Reservation(
            user_id=data.user_id,
            gift_id=data.gift_id,
            # cancelled_at по умолчанию NULL (активная)
        )
        self.db.add(reservation)
        self.db.commit()
        self.db.refresh(reservation)
        return reservation

    def cancel(self, reservation: Reservation, cancelled_at: datetime) -> Reservation:
        """Soft delete: пометить резервацию как отменённую"""
        reservation.cancelled_at = cancelled_at
        self.db.add(reservation)
        self.db.commit()
        self.db.refresh(reservation)
        return reservation

    def reactivate(self, reservation: Reservation) -> Reservation:
        """Реактивировать отменённую резервацию (cancelled_at = NULL)"""
        reservation.cancelled_at = None
        reservation.reserved_date = datetime.utcnow()  # Обновляем дату
        self.db.add(reservation)
        self.db.commit()
        self.db.refresh(reservation)
        return reservation

    def delete(self, reservation: Reservation) -> None:
        """Hard delete: физически удалить запись (админ-операция)"""
        self.db.delete(reservation)
        self.db.commit()
