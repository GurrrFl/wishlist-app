from typing import List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.dependencies import get_current_user, get_db
from app.models.user import User
from app.schemas.user import UserCreate, UserRead, UserShort, UserUpdate
from app.services.user_service import UserService


router = APIRouter(prefix="/users", tags=["Users"])


@router.post(
    "/register",
    response_model=UserRead,
    status_code=status.HTTP_201_CREATED,
    description="Регистрация нового пользователя"
)
def register_user(
    data: UserCreate,
    db: Session = Depends(get_db),
):
    service = UserService(db)
    try:
        user = service.register(data)
        return user
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post(
    "/login",
    description="Аутентификация пользователя"
)
def login_user(
    identifier: str,
    password: str,
    db: Session = Depends(get_db),
):
    service = UserService(db)
    try:
        user = service.authenticate(identifier, password)
        return {
            "access_token": str(user.user_id),
            "token_type": "bearer",
            "user": UserShort.model_validate(user)
        }
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))


@router.get(
    "/me",
    response_model=UserRead,
    description="Получить профиль текущего пользователя"
)
def get_current_user_profile(
    current_user: User = Depends(get_current_user),
):
    return current_user


@router.get(
    "/{user_id}",
    response_model=UserShort,
    description="Получить пользователя по ID"
)
def get_user_by_id(
    user_id: int,
    db: Session = Depends(get_db),
):
    service = UserService(db)
    user = service.get_by_id(user_id)
    if user is None:
        raise HTTPException(
            status_code=404,
            detail=f"User with id {user_id} not found"
        )
    return user


@router.get(
    "/",
    response_model=List[UserShort],
    description="Получить список пользователей"
)
def list_users(
    offset: int = 0,
    limit: int = 50,
    search: str = None,
    db: Session = Depends(get_db),
):
    service = UserService(db)
    users = service.list_users(offset=offset, limit=limit, search=search)
    return users


@router.patch(
    "/me",
    response_model=UserRead,
    description="Обновить профиль текущего пользователя"
)
def update_current_user_profile(
    data: UserUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = UserService(db)
    try:
        updated = service.update_profile(current_user.user_id, data)
        return updated
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.delete(
    "/me",
    status_code=status.HTTP_204_NO_CONTENT,
    description="Удалить текущего пользователя"
)
def delete_current_user(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    service = UserService(db)
    try:
        service.delete_user(current_user.user_id)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
