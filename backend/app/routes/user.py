from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from app.dependencies import get_db
from app.models.user import User
from app.schemas.user import UserCreate, UserRead, UserShort, UserUpdate
from app.services.user_service import UserService
from app.services.auth_service import create_access_token, get_current_user_from_token


router = APIRouter(prefix="/users", tags=["Users"])

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/users/token")


@router.post(
    "/register",
    response_model= UserShort,
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
    "/token",
    description="Получить JWT токен для аутентификации"
)
def login_for_access_token(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: Session = Depends(get_db),
):
    service = UserService(db)
    try:
        user = service.authenticate(form_data.username, form_data.password)
        access_token = create_access_token(data={"sub": str(user.user_id)})
        return {
            "access_token": access_token,
            "token_type": "bearer",
        }
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))


@router.get(
    "/me",
    response_model=UserRead,
    description="Получить профиль текущего пользователя"
)
def get_current_user_profile(
    current_user: User = Depends(get_current_user_from_token),
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
    current_user: User = Depends(get_current_user_from_token),
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
    current_user: User = Depends(get_current_user_from_token),
    db: Session = Depends(get_db),
):
    service = UserService(db)
    try:
        service.delete_user(current_user.user_id)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
