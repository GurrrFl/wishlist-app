from datetime import datetime
from typing import List, Optional

from sqlalchemy import Select, and_, select
from sqlalchemy.orm import Session

from app.models.reservation import Reservation
from app.schemas.reservation import ReservationCreate, ReservationUpdate


class ReservationRepository:
    def __init__(self, db: Session):
        self.db = db

    def _base_query(self) -> Select:
        return select(Reservation)

    def get_by_id(self, reservation_id: int) -> Optional[Reservation]:
        return self.db.get(Reservation, reservation_id)

    def get_by_user_and_gift(self, user_id: int, gift_id: int) -> Optional[Reservation]:
        stmt = self._base_query().where(
            and_(Reservation.user_id == user_id, Reservation.gift_id == gift_id)
        )
        return self.db.execute(stmt).scalars().first()

    def list_by_user(
        self,
        user_id: int,
        offset: int = 0,
        limit: int = 50,
        only_active: bool = False,
    ) -> List[Reservation]:
        stmt = self._base_query().where(Reservation.user_id == user_id)
        if only_active:
            stmt = stmt.where(Reservation.cancelled_at.is_(None))
        stmt = stmt.offset(offset).limit(limit)
        return list(self.db.execute(stmt).scalars().all())

    def list_by_gift(
        self,
        gift_id: int,
        offset: int = 0,
        limit: int = 50,
        only_active: bool = False,
    ) -> List[Reservation]:
        stmt = self._base_query().where(Reservation.gift_id == gift_id)
        if only_active:
            stmt = stmt.where(Reservation.cancelled_at.is_(None))
        stmt = stmt.offset(offset).limit(limit)
        return list(self.db.execute(stmt).scalars().all())

    def create(self, data: ReservationCreate) -> Reservation:
        reservation = Reservation(
            user_id=data.user_id,
            gift_id=data.gift_id,
        )
        self.db.add(reservation)
        self.db.commit()
        self.db.refresh(reservation)
        return reservation

    def update(self, reservation: Reservation, data: ReservationUpdate) -> Reservation:
        if data.cancelled_at is not None:
            reservation.cancelled_at = data.cancelled_at
        self.db.commit()
        self.db.refresh(reservation)
        return reservation

    def cancel(
        self,
        reservation: Reservation,
        cancelled_at: Optional[datetime] = None,
    ) -> Reservation:
        reservation.cancelled_at = cancelled_at or datetime.utcnow()
        self.db.commit()
        self.db.refresh(reservation)
        return reservation

    def delete(self, reservation: Reservation) -> None:
        self.db.delete(reservation)
        self.db.commit()
