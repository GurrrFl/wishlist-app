from sqlalchemy import Column, DateTime, ForeignKey, Integer, UniqueConstraint, func
from sqlalchemy.orm import relationship

from app.database import Base


class Reservation(Base):
    __tablename__ = "reservations"
    __table_args__ = (
        UniqueConstraint("user_id", "gift_id", name="uq_reservations_user_gift"),
    )

    reservation_id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.user_id"), nullable=False, index=True)
    gift_id = Column(Integer, ForeignKey("gifts.gift_id"), nullable=False, index=True)
    reserved_date = Column(DateTime(timezone=True), server_default=func.now())
    cancelled_at = Column(DateTime(timezone=True), nullable=True)

    user = relationship("User", back_populates="reservations")
    gift = relationship("Gift", back_populates="reservations")
