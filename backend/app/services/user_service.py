from typing import List, Optional
from passlib.context import CryptContext
from sqlalchemy.orm import Session
from ..models.user import User
from ..repositories.user_repository import UserRepository
from ..schemas.user import UserCreate, UserUpdate


pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


class UserService:
    def __init__(self, db: Session):
        self.db = db
        self.repo = UserRepository(db)

    def _hash_password(self, password: str) -> str:
        return pwd_context.hash(password)

    def _verify_password(self, plain_password: str, hashed_password: str) -> bool:
        return pwd_context.verify(plain_password, hashed_password)

    def register(self, data: UserCreate) -> User:
        if self.repo.get_by_login(data.login):
            raise ValueError("Login is already taken")
        if self.repo.get_by_email(data.email):
            raise ValueError("Email is already taken")
        password_hash = self._hash_password(data.password)
        return self.repo.create(data=data, password_hash=password_hash)

    def authenticate(self, identifier: str, password: str) -> User:
        user = self.repo.get_by_login(identifier)
        if user is None:
            user = self.repo.get_by_email(identifier)
        if user is None:
            raise ValueError("User not found")
        if not self._verify_password(password, user.password_hash):
            raise ValueError("Invalid credentials")
        return user

    def get_by_id(self, user_id: int) -> Optional[User]:
        return self.repo.get_by_id(user_id)

    def get_by_login(self, login: str) -> Optional[User]:
        return self.repo.get_by_login(login)

    def get_by_email(self, email: str) -> Optional[User]:
        return self.repo.get_by_email(email)

    def list_users(
        self,
        offset: int = 0,
        limit: int = 50,
        search: Optional[str] = None,
    ) -> List[User]:
        return self.repo.list(offset=offset, limit=limit, search=search)

    def update_profile(
        self,
        user_id: int,
        data: UserUpdate,
    ) -> User:
        user = self.repo.get_by_id(user_id)
        if user is None:
            raise ValueError("User not found")

        if data.login is not None and data.login != user.login:
            if self.repo.get_by_login(data.login):
                raise ValueError("Login is already taken")

        if data.email is not None and data.email != user.email:
            if self.repo.get_by_email(data.email):
                raise ValueError("Email is already taken")

        new_password_hash: Optional[str] = None
        if data.password is not None:
            new_password_hash = self._hash_password(data.password)

        return self.repo.update(user=user, data=data, new_password_hash=new_password_hash)

    def delete_user(self, user_id: int) -> None:
        user = self.repo.get_by_id(user_id)
        if user is None:
            raise ValueError("User not found")
        self.repo.delete(user)
