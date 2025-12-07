from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db
from app.schemas.user import UserCreate, UserRead, UserUpdate
from app.services.user_service import UserService

router = APIRouter(prefix="/users", tags=["users"])

@router.post("/", response_model=UserRead, status_code=status.HTTP_201_CREATED)
def create_user(user_data: UserCreate, db: Session = Depends(get_db)):
    """Create a new user"""
    service = UserService(db)
    try:
        user = service.register(user_data)
        return user
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.get("/{user_id}", response_model=UserRead)
def get_user(user_id: int, db: Session = Depends(get_db)):
    """Get user by ID"""
    service = UserService(db)
    user = service.get_by_id(user_id)
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return user

@router.put("/{user_id}", response_model=UserRead)
def update_user(user_id: int, user_data: UserUpdate, db: Session = Depends(get_db)):
    """Update user information"""
    service = UserService(db)
    try:
        user = service.update_profile(user_id, user_data)
        return user
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.delete("/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_user(user_id: int, db: Session = Depends(get_db)):
    """Delete a user"""
    service = UserService(db)
    try:
        service.delete_user(user_id)
        return
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))

@router.post("/login")
def login(identifier: str, password: str, db: Session = Depends(get_db)):
    """Authenticate user"""
    service = UserService(db)
    try:
        user = service.authenticate(identifier, password)
        return {"user_id": user.user_id, "login": user.login}
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))