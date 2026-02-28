from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field, HttpUrl
from typing_extensions import Literal


class GiftBase(BaseModel):
    name: str = Field(
        ...,
        min_length=1,
        max_length=150,
        description="Название подарка"
    )
    description: Optional[str] = Field(
        None,
        max_length=2000,
        description="Описание или пожелания по подарку"
    )
    price: Optional[float] = Field(
        None,
        ge=0,
        description="Ориентировочная стоимость подарка"
    )
    store_link: Optional[HttpUrl] = Field(
        None,
        description="Ссылка на магазин или пример подарка"
    )
    status: Optional[Literal["available", "reserved"]] = Field(
        "available",
        description="Текущий статус подарка: доступен или зарезервирован"
    )


class GiftCreate(GiftBase):
    wishlist_id: int = Field(
        ...,
        ge=1,
        description="ID списка, к которому привязан подарок"
    )


class GiftUpdate(BaseModel):
    name: Optional[str] = Field(
        None,
        min_length=1,
        max_length=150,
        description="Новое название подарка"
    )
    description: Optional[str] = Field(
        None,
        max_length=2000,
        description="Обновлённое описание подарка"
    )
    price: Optional[float] = Field(
        None,
        ge=0,
        description="Новая ориентировочная стоимость подарка"
    )
    store_link: Optional[HttpUrl] = Field(
        None,
        description="Новая ссылка на магазин или пример подарка"
    )
    status: Optional[Literal["available", "reserved"]] = Field(
        None,
        description="Новый статус подарка"
    )
    wishlist_id: Optional[int] = Field(
        None,
        ge=1,
        description="Новый ID списка, если подарок переносится"
    )


class GiftInDBBase(GiftBase):
    model_config = ConfigDict(from_attributes=True)

    gift_id: int = Field(..., description="Внутренний идентификатор подарка")
    wishlist_id: int = Field(
        ...,
        ge=1,
        description="ID списка, к которому сейчас привязан подарок"
    )
    created_at: datetime = Field(
        ...,
        description="Дата и время создания записи о подарке"
    )


class GiftShort(GiftInDBBase):
    pass


class GiftRead(GiftInDBBase):
    pass
