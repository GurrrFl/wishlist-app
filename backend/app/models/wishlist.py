from sqlalchemy import Column, Integer, String, Text, DateTime, Boolean, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.database import Base
from app.models.user import User

class Wishlist(Base):
    __tablename__ = "wishlists"

    wishlist_id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.user_id"), nullable=False)
    name = Column(String, nullable=False)
    event_date = Column(Text, nullable=False)
    is_private = Column(Integer, default=0)
    unique_link = Column(String, unique=True, index=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="wishlists")
    gifts = relationship("Gift", back_populates="wishlist", cascade="all, delete-orphan")