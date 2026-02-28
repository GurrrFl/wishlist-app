from typing import List, Optional
from sqlalchemy.orm import Session
from ..models.gift import Gift
from ..repositories.gift_repository import GiftRepository
from ..repositories.wishlist_repository import WishlistRepository
from ..schemas.gift import GiftCreate, GiftUpdate


class GiftService:
    def __init__(self, db: Session):
        self.db = db
        self.gift_repo = GiftRepository(db)
        self.wishlist_repo = WishlistRepository(db)

    def _ensure_wishlist_owner(self, wishlist_id: int, user_id: int) -> None:
        wishlist = self.wishlist_repo.get_by_id(wishlist_id)
        if wishlist is None:
            raise ValueError("Wishlist not found")
        if wishlist.user_id != user_id:
            raise PermissionError("Access denied to wishlist")

    def create_for_user(
        self,
        owner_id: int,
        data: GiftCreate,
    ) -> Gift:
        self._ensure_wishlist_owner(data.wishlist_id, owner_id)
        gift = self.gift_repo.create(data)
        return gift

    def get_for_owner(self, gift_id: int, owner_id: int) -> Gift:
        gift = self.gift_repo.get_by_id(gift_id)
        if not gift:
            raise ValueError("Gift not found")
        self._ensure_wishlist_owner(gift.wishlist_id, owner_id)
        return gift

    def list_for_wishlist(
        self,
        owner_id: int,
        wishlist_id: int,
        offset: int = 0,
        limit: int = 50,
        status: Optional[str] = None,
        search: Optional[str] = None,
    ) -> List[Gift]:
        self._ensure_wishlist_owner(wishlist_id, owner_id)
        return self.gift_repo.list_by_wishlist(
            wishlist_id=wishlist_id,
            offset=offset,
            limit=limit,
            status=status,
            search=search,
        )

    def update_for_owner(
        self,
        gift_id: int,
        owner_id: int,
        data: GiftUpdate,
    ) -> Gift:
        gift = self.get_for_owner(gift_id, owner_id)

        if data.wishlist_id is not None and data.wishlist_id != gift.wishlist_id:
            self._ensure_wishlist_owner(data.wishlist_id, owner_id)

        updated = self.gift_repo.update(gift, data)
        return updated

    def delete_for_owner(self, gift_id: int, owner_id: int) -> None:
        gift = self.get_for_owner(gift_id, owner_id)
        if gift.status == "reserved":
            raise ValueError("Cannot delete reserved gift")
        self.gift_repo.delete(gift)

    def change_status_for_owner(
        self,
        gift_id: int,
        owner_id: int,
        new_status: str,
    ) -> Gift:
        gift = self.get_for_owner(gift_id, owner_id)
        if new_status not in ("available", "reserved"):
            raise ValueError("Invalid gift status")
        return self.gift_repo.change_status(gift, new_status)
