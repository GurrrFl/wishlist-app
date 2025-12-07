from sqlalchemy import Column, Integer, String, Text, DateTime, Float, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.database import Base
from app.models.wishlist import Wishlist

class Gift(Base):
    __tablename__ = "gifts"

    gift_id = Column(Integer, primary_key=True, index=True)
    wishlist_id = Column(Integer, ForeignKey("wishlists.wishlist_id"), nullable=False)
    name = Column(String, nullable=False)
    description = Column(Text)
    price = Column(Float)
    store_link = Column(String)
    status = Column(String, default="available", server_default="available")
    created_at = Column(DateTime(timezone=True), server_default=func.now())

  
    wishlist = relationship("Wishlist", back_populates="gifts")
    reservations = relationship("Reservation", back_populates="gift", cascade="all, delete-orphan")