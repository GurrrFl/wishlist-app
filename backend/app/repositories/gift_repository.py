from typing import List, Optional

from sqlalchemy import Select, and_, select
from sqlalchemy.orm import Session

from app.models.gift import Gift
from app.schemas.gift import GiftCreate, GiftUpdate


class GiftRepository:
    def __init__(self, db: Session):
        self.db = db

    def _base_query(self) -> Select:
        return select(Gift)

    def get_by_id(self, gift_id: int) -> Optional[Gift]:
        return self.db.get(Gift, gift_id)

    def list_by_wishlist(
        self,
        wishlist_id: int,
        offset: int = 0,
        limit: int = 50,
        status: Optional[str] = None,
        search: Optional[str] = None,
    ) -> List[Gift]:
        stmt = self._base_query().where(Gift.wishlist_id == wishlist_id)
        conditions = []
        if status is not None:
            conditions.append(Gift.status == status)
        if search:
            like = f"%{search}%"
            conditions.append(Gift.name.ilike(like))
        if conditions:
            stmt = stmt.where(and_(*conditions))
        stmt = stmt.offset(offset).limit(limit)
        return list(self.db.execute(stmt).scalars().all())

    def create(self, data: GiftCreate) -> Gift:
        gift = Gift(
            wishlist_id=data.wishlist_id,
            name=data.name,
            description=data.description,
            price=data.price,
            store_link=str(data.store_link) if data.store_link is not None else None,
            status=data.status or "available",
        )
        self.db.add(gift)
        self.db.commit()
        self.db.refresh(gift)
        return gift

    def update(self, gift: Gift, data: GiftUpdate) -> Gift:
        if data.name is not None:
            gift.name = data.name
        if data.description is not None:
            gift.description = data.description
        if data.price is not None:
            gift.price = data.price
        if data.store_link is not None:
            gift.store_link = str(data.store_link)
        if data.status is not None:
            gift.status = data.status
        if data.wishlist_id is not None:
            gift.wishlist_id = data.wishlist_id
        self.db.commit()
        self.db.refresh(gift)
        return gift

    def delete(self, gift: Gift) -> None:
        self.db.delete(gift)
        self.db.commit()

    def change_status(self, gift: Gift, new_status: str) -> Gift:
        gift.status = new_status
        self.db.commit()
        self.db.refresh(gift)
        return gift
