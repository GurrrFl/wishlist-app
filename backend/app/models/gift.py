from sqlalchemy import Column, Integer, String, Text, DateTime, Float, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.database import Base
from app.models.wishlist import Wishlist
from datetime import datetime
def now_str() -> str:
    return datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")

class Gift(Base):
    __tablename__ = "gifts"

    gift_id = Column(Integer, primary_key=True, index=True)
    wishlist_id = Column(Integer, ForeignKey("wishlists.wishlist_id"), nullable=False)
    name = Column(String, nullable=False)
    description = Column(Text)
    price = Column(Float)
    store_link = Column(String)
    status = Column(String, default="available", server_default="available")
    created_at = Column(String, nullable=False, default=now_str)

  
    wishlist = relationship("Wishlist", back_populates="gifts")
    reservations = relationship("Reservation", back_populates="gift", cascade="all, delete-orphan")