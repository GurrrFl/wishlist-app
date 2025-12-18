from typing import List, Optional
from sqlalchemy import select
from sqlalchemy.orm import Session
from ..models.user import User
from ..schemas.user import UserCreate, UserUpdate


class UserRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_by_id(self, user_id: int) -> Optional[User]:
        return self.db.get(User, user_id)

    def get_by_login(self, login: str) -> Optional[User]:
        stmt = select(User).where(User.login == login)
        return self.db.execute(stmt).scalars().first()

    def get_by_email(self, email: str) -> Optional[User]:
        stmt = select(User).where(User.email == email)
        return self.db.execute(stmt).scalars().first()

    def list(
        self,
        offset: int = 0,
        limit: int = 50,
        search: Optional[str] = None,
    ) -> List[User]:
        stmt = select(User)
        if search:
            like = f"%{search}%"
            stmt = stmt.where((User.login.ilike(like)) | (User.email.ilike(like)))
        stmt = stmt.offset(offset).limit(limit)
        return list(self.db.execute(stmt).scalars().all())

    def create(self, data: UserCreate, password_hash: str) -> User:
        user = User(
            login=data.login,
            email=data.email,
            password_hash=password_hash,
        )
        self.db.add(user)
        self.db.commit()
        self.db.refresh(user)
        return user

    def update(self, user: User, data: UserUpdate, new_password_hash: Optional[str] = None) -> User:
        if data.login is not None:
            user.login = data.login
        if data.email is not None:
            user.email = data.email
        if new_password_hash is not None:
            user.password_hash = new_password_hash
        self.db.commit()
        self.db.refresh(user)
        return user

    def delete(self, user: User) -> None:
        self.db.delete(user)
        self.db.commit()
