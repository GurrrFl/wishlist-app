from typing import Generator

from fastapi import Depends
from sqlalchemy.orm import Session

from app.database import SessionLocal
from app.models.user import User


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def get_current_user(db: Session = Depends(get_db)) -> User:

    from app.repositories.user_repository import UserRepository
    
    user_repo = UserRepository(db)
    user = user_repo.get_by_id(1)
    
    if user is None:
        from app.schemas.user import UserCreate
        from app.services.user_service import UserService
        
        service = UserService(db)
        test_data = UserCreate(
            login="testuser",
            email="test@example.com",
            password="testpassword123"
        )
        user = service.register(test_data)
    
    return user
