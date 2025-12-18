from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict, EmailStr, Field


class UserBase(BaseModel):
    login: str = Field(
        ...,
        min_length=3,
        max_length=50,
        description="Уникальный логин пользователя"
    )
    email: EmailStr = Field(
        ...,
        description="Электронная почта пользователя"
    )


class UserCreate(UserBase):
    password: str = Field(
        ...,
        min_length=8,
        max_length=128,
        description="Пароль в открытом виде, который будет захеширован"
    )


class UserUpdate(BaseModel):
    login: Optional[str] = Field(
        None,
        min_length=3,
        max_length=50,
        description="Новый логин пользователя"
    )
    email: Optional[EmailStr] = Field(
        None,
        description="Новый адрес электронной почты"
    )
    password: Optional[str] = Field(
        None,
        min_length=8,
        max_length=128,
        description="Новый пароль пользователя"
    )


class UserInDBBase(UserBase):
    model_config = ConfigDict(from_attributes=True)

    user_id: int = Field(..., description="Внутренний идентификатор пользователя")
    created_at: datetime = Field(..., description="Дата и время создания пользователя")


class UserShort(UserInDBBase):
    pass


class UserRead(UserInDBBase):
    wishlists: List["WishlistShort"] = Field(
        default_factory=list,
        description="Список вишлистов пользователя"
    )

from .wishlist import WishlistShort

