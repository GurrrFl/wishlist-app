import uuid
from typing import List, Optional
from sqlalchemy.orm import Session
from ..models.wishlist import Wishlist
from ..repositories.wishlist_repository import WishlistRepository
from ..schemas.wishlist import WishlistCreate, WishlistUpdate


class WishlistService:
    def __init__(self, db: Session):
        self.db = db
        self.repo = WishlistRepository(db)

    def _generate_unique_link(self) -> str:
        return uuid.uuid4().hex

    def create_for_user(
        self,
        user_id: int,
        data: WishlistCreate,
        generate_link_if_missing: bool = True,
    ) -> Wishlist:
        unique_link = data.unique_link
        if unique_link is None and generate_link_if_missing and not data.is_private:
            unique_link = self._generate_unique_link()

        payload = WishlistCreate(
            user_id=user_id,
            name=data.name,
            event_date=data.event_date,
            is_private=data.is_private,
            unique_link=unique_link,
        )
        return self.repo.create(payload)

    def get_for_owner(self, wishlist_id: int, owner_id: int) -> Wishlist:
        wishlist = self.repo.get_by_id(wishlist_id)
        if wishlist is None:
            raise ValueError("Wishlist not found")
        if wishlist.user_id != owner_id:
            raise PermissionError("Access denied")
        return wishlist

    def get_public_by_link(self, unique_link: str) -> Optional[Wishlist]:
        return self.repo.get_by_unique_link(unique_link=unique_link, only_public=True)

    def list_for_user(
        self,
        user_id: int,
        offset: int = 0,
        limit: int = 50,
        include_private: bool = True,
        search: Optional[str] = None,
    ) -> List[Wishlist]:
        return self.repo.list_by_user(
            user_id=user_id,
            offset=offset,
            limit=limit,
            include_private=include_private,
            search=search,
        )

    def update_for_user(
        self,
        wishlist_id: int,
        owner_id: int,
        data: WishlistUpdate,
    ) -> Wishlist:
        wishlist = self.get_for_owner(wishlist_id=wishlist_id, owner_id=owner_id)

        if data.is_private is True:
            if data.unique_link is not None:
                wishlist = self.repo.set_unique_link(wishlist, None)

        updated = self.repo.update(wishlist=wishlist, data=data)
        return updated

    def delete_for_user(self, wishlist_id: int, owner_id: int) -> None:
        wishlist = self.get_for_owner(wishlist_id=wishlist_id, owner_id=owner_id)
        self.repo.delete(wishlist)

    def regenerate_unique_link(self, wishlist_id: int, owner_id: int) -> Wishlist:
        wishlist = self.get_for_owner(wishlist_id=wishlist_id, owner_id=owner_id)
        if wishlist.is_private:
            raise ValueError("Cannot set unique link for private wishlist")
        new_link = self._generate_unique_link()
        return self.repo.set_unique_link(wishlist, new_link)

    def clear_unique_link(self, wishlist_id: int, owner_id: int) -> Wishlist:
        wishlist = self.get_for_owner(wishlist_id=wishlist_id, owner_id=owner_id)
        return self.repo.set_unique_link(wishlist, None)
