from typing import List, Optional

from sqlalchemy import and_, select
from sqlalchemy.orm import Session

from app.models.wishlist import Wishlist
from app.schemas.wishlist import WishlistCreate, WishlistUpdate


class WishlistRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_by_id(self, wishlist_id: int) -> Optional[Wishlist]:
        return self.db.get(Wishlist, wishlist_id)

    def get_by_unique_link(self, unique_link: str, only_public: bool = True) -> Optional[Wishlist]:
        conditions = [Wishlist.unique_link == unique_link]
        if only_public:
            conditions.append(Wishlist.is_private == 0)
        stmt = select(Wishlist).where(and_(*conditions))
        return self.db.execute(stmt).scalars().first()

    def list_by_user(
        self,
        user_id: int,
        offset: int = 0,
        limit: int = 50,
        include_private: bool = True,
        search: Optional[str] = None,
    ) -> List[Wishlist]:
        stmt = select(Wishlist).where(Wishlist.user_id == user_id)
        if not include_private:
            stmt = stmt.where(Wishlist.is_private == 0)
        if search:
            like = f"%{search}%"
            stmt = stmt.where(Wishlist.name.ilike(like))
        stmt = stmt.offset(offset).limit(limit)
        return list(self.db.execute(stmt).scalars().all())

    def create(self, data: WishlistCreate) -> Wishlist:
        wishlist = Wishlist(
            user_id=data.user_id,
            name=data.name,
            event_date=str(data.event_date),
            is_private=1 if data.is_private else 0,
            unique_link=data.unique_link,
        )
        self.db.add(wishlist)
        self.db.commit()
        self.db.refresh(wishlist)
        return wishlist

    def update(self, wishlist: Wishlist, data: WishlistUpdate) -> Wishlist:
        if data.name is not None:
            wishlist.name = data.name
        if data.event_date is not None:
            wishlist.event_date = str(data.event_date)
        if data.is_private is not None:
            wishlist.is_private = 1 if data.is_private else 0
        if data.unique_link is not None:
            wishlist.unique_link = data.unique_link
        self.db.commit()
        self.db.refresh(wishlist)
        return wishlist

    def delete(self, wishlist: Wishlist) -> None:
        self.db.delete(wishlist)
        self.db.commit()

    def set_unique_link(self, wishlist: Wishlist, unique_link: Optional[str]) -> Wishlist:
        wishlist.unique_link = unique_link
        self.db.commit()
        self.db.refresh(wishlist)
        return wishlist

    def is_owner(self, wishlist_id: int, user_id: int) -> bool:
        stmt = select(Wishlist.wishlist_id).where(
            (Wishlist.wishlist_id == wishlist_id) & (Wishlist.user_id == user_id)
        )
        return self.db.execute(stmt).scalar_one_or_none() is not None
