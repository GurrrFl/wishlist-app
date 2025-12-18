from datetime import datetime
from typing import List, Optional
from sqlalchemy.orm import Session
from ..models.reservation import Reservation
from ..repositories.gift_repository import GiftRepository
from ..repositories.reservation_repository import ReservationRepository
from ..repositories.wishlist_repository import WishlistRepository
from ..schemas.reservation import ReservationCreate, ReservationUpdate


class ReservationService:
    def __init__(self, db: Session):
        self.db = db
        self.reservation_repo = ReservationRepository(db)
        self.gift_repo = GiftRepository(db)
        self.wishlist_repo = WishlistRepository(db)

    def _ensure_can_access_gift(self, gift_id: int, user_id: int) -> None:
        gift = self.gift_repo.get_by_id(gift_id)
        if gift is None:
            raise ValueError("Gift not found")
        wishlist = self.wishlist_repo.get_by_id(gift.wishlist_id)
        if wishlist is None:
            raise ValueError("Wishlist not found")
        if wishlist.is_private and wishlist.user_id != user_id:
            raise PermissionError("Access denied to gift")

    def reserve_gift(self, user_id: int, gift_id: int) -> Reservation:
        gift = self.gift_repo.get_by_id(gift_id)
        if gift is None:
            raise ValueError("Gift not found")

        self._ensure_can_access_gift(gift_id, user_id)

        if gift.status != "available":
            raise ValueError("Gift is already reserved")

        if gift.wishlist.user_id == user_id:
            raise ValueError("Cannot reserve your own gift")

        existing = self.reservation_repo.get_by_user_and_gift(user_id, gift_id)

        if existing:
            if existing.cancelled_at is None:
                raise ValueError("Gift is already reserved by this user")

            return self.reservation_repo.reactivate(existing)

        data = ReservationCreate(user_id=user_id, gift_id=gift_id)
        return self.reservation_repo.create(data)

    def cancel_for_user(self, reservation_id: int, user_id: int) -> Reservation:
        reservation = self.reservation_repo.get_by_id(reservation_id)
        if reservation is None:
            raise ValueError("Reservation not found")
        if reservation.user_id != user_id:
            raise PermissionError("Access denied to reservation")
        if reservation.cancelled_at is not None:
            return reservation 
        return self.reservation_repo.cancel(reservation, datetime.utcnow())

    def get_for_user(
        self,
        reservation_id: int,
        user_id: int,
    ) -> Reservation:
        reservation = self.reservation_repo.get_by_id(reservation_id)
        if reservation is None:
            raise ValueError("Reservation not found")
        if reservation.user_id != user_id:
            raise PermissionError("Access denied to reservation")
        return reservation

    def list_for_user(
        self,
        user_id: int,
        offset: int = 0,
        limit: int = 50,
        only_active: bool = False,
    ) -> List[Reservation]:
        return self.reservation_repo.list_by_user(
            user_id=user_id,
            offset=offset,
            limit=limit,
            only_active=only_active,
        )

    def list_for_gift(
        self,
        gift_id: int,
        owner_id: int,
        offset: int = 0,
        limit: int = 50,
        only_active: bool = False,
    ) -> List[Reservation]:
        gift = self.gift_repo.get_by_id(gift_id)
        if gift is None:
            raise ValueError("Gift not found")
        wishlist = self.wishlist_repo.get_by_id(gift.wishlist_id)
        if wishlist is None:
            raise ValueError("Wishlist not found")
        if wishlist.user_id != owner_id:
            raise PermissionError("Access denied to reservations for this gift")

        return self.reservation_repo.list_by_gift(
            gift_id=gift_id,
            offset=offset,
            limit=limit,
            only_active=only_active,
        )



    def admin_delete(self, reservation_id: int) -> None:
        reservation = self.reservation_repo.get_by_id(reservation_id)
        if reservation is None:
            raise ValueError("Reservation not found")
        self.reservation_repo.delete(reservation)
