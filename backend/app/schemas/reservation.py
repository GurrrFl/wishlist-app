from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field


class ReservationBase(BaseModel):
    user_id: int = Field(
        ...,
        ge=1,
        description="ID пользователя, который бронирует подарок"
    )
    gift_id: int = Field(
        ...,
        ge=1,
        description="ID подарка, который резервируется"
    )


class ReservationCreate(ReservationBase):
    pass


class ReservationUpdate(BaseModel):
    cancelled_at: Optional[datetime] = Field(
        None,
        description="Момент времени, когда бронь была отменена"
    )


class ReservationInDBBase(ReservationBase):
    model_config = ConfigDict(from_attributes=True)

    reservation_id: int = Field(
        ...,
        description="Внутренний идентификатор брони"
    )
    reserved_date: datetime = Field(
        ...,
        description="Дата и время создания брони"
    )
    cancelled_at: Optional[datetime] = Field(
        None,
        description="Фактическое время отмены брони, если она отменена"
    )


class ReservationRead(ReservationInDBBase):
    pass
