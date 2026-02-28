import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.database import Base
from app.schemas.user import UserCreate
from app.schemas.wishlist import WishlistCreate
from app.schemas.gift import GiftCreate
from app.services.user_service import UserService
from app.services.wishlist_service import WishlistService
from app.services.gift_service import GiftService
from app.services.reservation_service import ReservationService


@pytest.fixture
def db_session():
    engine = create_engine("sqlite:///:memory:")
    Base.metadata.create_all(engine)
    SessionLocal = sessionmaker(bind=engine)
    session = SessionLocal()
    try:
        yield session
    finally:
        session.close()


@pytest.fixture
def owner_user(db_session):
    service = UserService(db_session)
    data = UserCreate(
        login="owner",
        email="owner@example.com",
        password="password123"
    )
    return service.register(data)


@pytest.fixture
def other_user(db_session):
    service = UserService(db_session)
    data = UserCreate(
        login="other",
        email="other@example.com",
        password="password123"
    )
    return service.register(data)


@pytest.fixture
def owner_wishlist(db_session, owner_user):
    service = WishlistService(db_session)
    data = WishlistCreate(
        user_id=owner_user.user_id,
        name="Owner Wishlist",
        event_date="2025-12-31",
        is_private=False
    )
    return service.create_for_user(owner_user.user_id, data)


@pytest.fixture
def owner_gift(db_session, owner_user, owner_wishlist):
    service = GiftService(db_session)
    data = GiftCreate(
        wishlist_id=owner_wishlist.wishlist_id,
        name="Owner Gift",
        price=100.0
    )
    return service.create_for_user(owner_user.user_id, data)


@pytest.fixture
def reservation_service(db_session):
    return ReservationService(db_session)


class TestReservationCreate:
    """Тесты создания резервации"""

    def test_reserve_gift_success(self, db_session, owner_gift, other_user, reservation_service):
        """✅ Позитив: другой пользователь успешно резервирует подарок"""
        reservation = reservation_service.reserve_gift(
            user_id=other_user.user_id,
            gift_id=owner_gift.gift_id,
        )

        assert reservation.reservation_id is not None
        assert reservation.user_id == other_user.user_id
        assert reservation.gift_id == owner_gift.gift_id
        

    def test_reserve_gift_twice_by_same_user(self, db_session, owner_gift, other_user, reservation_service):
        """❌ Негатив: один и тот же пользователь пытается зарезервировать один подарок дважды"""
        reservation_service.reserve_gift(
            user_id=other_user.user_id,
            gift_id=owner_gift.gift_id,
        )

        with pytest.raises(ValueError, match="already reserved by this user"):
            reservation_service.reserve_gift(
                user_id=other_user.user_id,
                gift_id=owner_gift.gift_id,
            )

    def test_reserve_gift_by_owner_forbidden(self, db_session, owner_gift, owner_user, reservation_service):
        """❌ Негатив: владелец списка пытается зарезервировать свой же подарок"""
        with pytest.raises(ValueError, match="Cannot reserve your own gift"):
            reservation_service.reserve_gift(
                user_id=owner_user.user_id,
                gift_id=owner_gift.gift_id,
            )

    def test_reserve_nonexistent_gift(self, db_session, other_user, reservation_service):
        """❌ Негатив: резервирование несуществующего подарка"""
        with pytest.raises(ValueError, match="Gift not found"):
            reservation_service.reserve_gift(
                user_id=other_user.user_id,
                gift_id=99999,
            )


class TestReservationList:
    """Тесты получения списков резерваций"""

    def test_list_reservations_for_user(self, db_session, owner_gift, other_user, reservation_service):
        """✅ Позитив: список резерваций пользователя"""
        reservation_service.reserve_gift(
            user_id=other_user.user_id,
            gift_id=owner_gift.gift_id,
        )

        reservations = reservation_service.list_for_user(
            user_id=other_user.user_id,
            offset=0,
            limit=10,
            only_active=False,
        )

        assert len(reservations) == 1
        assert reservations[0].user_id == other_user.user_id

    def test_list_reservations_for_user_empty(self, db_session, other_user, reservation_service):
        """✅ Позитив: пустой список резерваций у пользователя без броней"""
        reservations = reservation_service.list_for_user(
            user_id=other_user.user_id,
            offset=0,
            limit=10,
            only_active=True,
        )

        assert reservations == []

    def test_list_reservations_for_gift_as_owner(self, db_session, owner_user, owner_gift, other_user, reservation_service):
        """✅ Позитив: владелец видит резервации своего подарка"""
        reservation_service.reserve_gift(
            user_id=other_user.user_id,
            gift_id=owner_gift.gift_id,
        )

        reservations = reservation_service.list_for_gift(
            gift_id=owner_gift.gift_id,
            owner_id=owner_user.user_id,
            offset=0,
            limit=10,
            only_active=True,
        )

        assert len(reservations) == 1
        assert reservations[0].gift_id == owner_gift.gift_id

    def test_list_reservations_for_gift_not_owner_forbidden(self, db_session, owner_gift, other_user, reservation_service):
        """❌ Негатив: чужой пользователь пытается посмотреть резервации чужого подарка"""
        with pytest.raises(PermissionError):
            reservation_service.list_for_gift(
                gift_id=owner_gift.gift_id,
                owner_id=other_user.user_id,
                offset=0,
                limit=10,
                only_active=True,
            )


class TestReservationCancel:
    """Тесты отмены резерваций"""

    def test_cancel_reservation_success(self, db_session, owner_gift, other_user, reservation_service):
        """✅ Позитив: успешная отмена своей резервации"""
        reservation = reservation_service.reserve_gift(
            user_id=other_user.user_id,
            gift_id=owner_gift.gift_id,
        )

        cancelled = reservation_service.cancel_for_user(
            reservation_id=reservation.reservation_id,
            user_id=other_user.user_id,
        )

        assert cancelled.cancelled_at is not None

        # повторная отмена возвращает тот же объект без ошибки
        cancelled_again = reservation_service.cancel_for_user(
            reservation_id=reservation.reservation_id,
            user_id=other_user.user_id,
        )
        assert cancelled_again.cancelled_at == cancelled.cancelled_at

    def test_cancel_reservation_not_owner_forbidden(self, db_session, owner_gift, owner_user, other_user, reservation_service):
        """❌ Негатив: один пользователь пытается отменить бронь другого"""
        reservation = reservation_service.reserve_gift(
            user_id=other_user.user_id,
            gift_id=owner_gift.gift_id,
        )

        with pytest.raises(PermissionError):
            reservation_service.cancel_for_user(
                reservation_id=reservation.reservation_id,
                user_id=owner_user.user_id,
            )

    def test_cancel_nonexistent_reservation(self, db_session, other_user, reservation_service):
        """❌ Негатив: отмена несуществующей резервации"""
        with pytest.raises(ValueError, match="Reservation not found"):
            reservation_service.cancel_for_user(
                reservation_id=99999,
                user_id=other_user.user_id,
            )


class TestReservationGet:
    """Тесты получения одной резервации"""

    def test_get_reservation_for_owner(self, db_session, owner_gift, other_user, reservation_service):
        """✅ Позитив: пользователь получает свою резервацию по ID"""
        reservation = reservation_service.reserve_gift(
            user_id=other_user.user_id,
            gift_id=owner_gift.gift_id,
        )

        fetched = reservation_service.get_for_user(
            reservation_id=reservation.reservation_id,
            user_id=other_user.user_id,
        )

        assert fetched.reservation_id == reservation.reservation_id
        assert fetched.user_id == other_user.user_id

    def test_get_reservation_of_another_user_forbidden(self, db_session, owner_gift, owner_user, other_user, reservation_service):
        """❌ Негатив: попытка получить резервацию другого пользователя"""
        reservation = reservation_service.reserve_gift(
            user_id=other_user.user_id,
            gift_id=owner_gift.gift_id,
        )

        with pytest.raises(PermissionError):
            reservation_service.get_for_user(
                reservation_id=reservation.reservation_id,
                user_id=owner_user.user_id,
            )

    def test_get_nonexistent_reservation(self, db_session, other_user, reservation_service):
        """❌ Негатив: получение несуществующей резервации"""
        with pytest.raises(ValueError, match="Reservation not found"):
            reservation_service.get_for_user(
                reservation_id=99999,
                user_id=other_user.user_id,
            )
