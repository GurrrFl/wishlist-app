from sqlalchemy import Column, Integer, String, Text, Boolean, Date, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.database import Base
from app.models.user import User
from datetime import datetime
def now_str() -> str:
    return datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")

class Wishlist(Base):
    __tablename__ = "wishlists"

    wishlist_id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.user_id"), nullable=False)
    name = Column(String, nullable=False)
    event_date =Column(String, nullable=False, default=now_str)
    is_private = Column(Integer, default=0)
    unique_link = Column(String, unique=True, index=True)
    created_at = Column(String, nullable=False, default=now_str)

    user = relationship("User", back_populates="wishlists")
    gifts = relationship("Gift", back_populates="wishlist", cascade="all, delete-orphan")