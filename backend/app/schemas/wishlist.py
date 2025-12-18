from datetime import date, datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field


class WishlistBase(BaseModel):
    name: str = Field(
        ...,
        min_length=1,
        max_length=100,
        description="Название списка пожеланий"
    )
    event_date: date = Field(
        ...,
        description="Дата события, к которому относится список подарков"
    )
    is_private: bool = Field(
        False,
        description="Признак приватного списка (True) или публичного (False)"
    )


class WishlistCreate(WishlistBase):
    user_id: int = Field(
        ...,
        ge=1,
        description="ID пользователя-владельца списка"
    )
    unique_link: Optional[str] = Field(
        None,
        max_length=255,
        description="Уникальная ссылка для доступа к списку"
    )


class WishlistUpdate(BaseModel):
    name: Optional[str] = Field(
        None,
        min_length=1,
        max_length=100,
        description="Новое название списка пожеланий"
    )
    event_date: Optional[date] = Field(
        None,
        description="Новая дата события для списка"
    )
    is_private: Optional[bool] = Field(
        None,
        description="Новый признак приватности списка"
    )
    unique_link: Optional[str] = Field(
        None,
        max_length=255,
        description="Обновлённая уникальная ссылка для доступа к списку"
    )


class WishlistInDBBase(WishlistBase):
    model_config = ConfigDict(from_attributes=True)

    wishlist_id: int = Field(..., description="Внутренний идентификатор списка")
    user_id: int = Field(..., ge=1, description="ID владельца списка")
    unique_link: Optional[str] = Field(
        None,
        description="Текущая уникальная ссылка на список"
    )
    created_at: datetime = Field(
        ...,
        description="Дата и время создания списка"
    )


class WishlistShort(WishlistInDBBase):
    pass


class WishlistRead(WishlistInDBBase):
    gifts: List["GiftShort"] = Field(
        default_factory=list,
        description="Список подарков, входящих в этот вишлист"
    )

from .gift import GiftShort
