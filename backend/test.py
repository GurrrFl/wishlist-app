import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.database import Base
from app.schemas.user import UserCreate
from app.schemas.gift import GiftCreate
from app.schemas.wishlist import WishlistCreate
from app.services.user_service import UserService
from app.services.gift_service import GiftService
from app.services.wishlist_service import WishlistService
from app.services.reservation_service import ReservationService


@pytest.fixture
def db_session():
    """Создаёт временную in-memory БД для каждого теста"""
    engine = create_engine("sqlite:///:memory:")
    Base.metadata.create_all(engine)
    SessionLocal = sessionmaker(bind=engine)
    session = SessionLocal()
    yield session
    session.close()


@pytest.fixture
def test_user(db_session):
    """Создаёт тестового пользователя"""
    service = UserService(db_session)
    user_data = UserCreate(
        login="testuser",
        email="test@example.com",
        password="password123"
    )
    return service.register(user_data)


@pytest.fixture
def test_wishlist(db_session, test_user):
    """Создаёт тестовый вишлист"""
    service = WishlistService(db_session)
    wishlist_data = WishlistCreate(
        user_id=test_user.user_id,
        name="Test Wishlist",
        event_date="2025-12-31",
        is_private=False
    )
    return service.create_for_user(test_user.user_id, wishlist_data)


@pytest.fixture
def test_gift(db_session, test_user, test_wishlist):
    """Создаёт тестовый подарок"""
    service = GiftService(db_session)
    gift_data = GiftCreate(
        wishlist_id=test_wishlist.wishlist_id,
        name="Test Gift",
        description="Description",
        price=99.99
    )
    return service.create_for_user(test_user.user_id, gift_data)


class TestUserService:
    """Тесты для UserService.register() и authenticate()"""
    
    def test_register_valid_user(self, db_session):
        """Тест 1.1: Регистрация с валидными данными"""
        service = UserService(db_session)
        user_data = UserCreate(
            login="newuser",
            email="new@example.com",
            password="securepass"
        )
        user = service.register(user_data)
        
        assert user.login == "newuser"
        assert user.email == "new@example.com"
        assert user.user_id is not None
    
    def test_register_duplicate_login(self, db_session, test_user):
        """Тест 1.2: Регистрация с существующим логином (исключение)"""
        service = UserService(db_session)
        user_data = UserCreate(
            login=test_user.login,
            email="another@example.com",
            password="pass8888888"
        )
        
        with pytest.raises(ValueError, match="Login is already taken"):
            service.register(user_data)
    
    def test_authenticate_valid_credentials(self, db_session, test_user):
        """Тест 2.1: Аутентификация с правильными данными"""
        service = UserService(db_session)
        user = service.authenticate("testuser", "password123")
        
        assert user.user_id == test_user.user_id
    
    def test_authenticate_invalid_password(self, db_session, test_user):
        """Тест 2.2: Аутентификация с неверным паролем (исключение)"""
        service = UserService(db_session)
        
        with pytest.raises(ValueError, match="Invalid credentials"):
            service.authenticate("testuser", "wrongpassword")
    
    def test_authenticate_nonexistent_user(self, db_session):
        """Тест 2.3: Аутентификация несуществующего пользователя"""
        service = UserService(db_session)
        
        with pytest.raises(ValueError, match="User not found"):
            service.authenticate("ghost", "password")


class TestGiftRepository:
    """Тесты для GiftRepository.list_by_wishlist()"""
    
    def test_list_by_wishlist_returns_list(self, db_session, test_user, test_wishlist, test_gift):
        """Тест 3.1: Получение списка подарков вишлиста"""
        from app.repositories.gift_repository import GiftRepository
        repo = GiftRepository(db_session)
        
        gifts = repo.list_by_wishlist(test_wishlist.wishlist_id)
        
        assert isinstance(gifts, list)
        assert len(gifts) == 1
        assert gifts[0].gift_id == test_gift.gift_id
    
    def test_list_by_wishlist_empty(self, db_session, test_user, test_wishlist):
        """Тест 3.2: Пустой список для вишлиста без подарков"""
        from app.repositories.gift_repository import GiftRepository
        repo = GiftRepository(db_session)
        
        gifts = repo.list_by_wishlist(test_wishlist.wishlist_id)
        
        assert gifts == []
    
    def test_list_by_wishlist_with_status_filter(self, db_session, test_user, test_wishlist, test_gift):
        """Тест 3.3: Фильтрация по статусу"""
        from app.repositories.gift_repository import GiftRepository
        repo = GiftRepository(db_session)
        
        gifts = repo.list_by_wishlist(test_wishlist.wishlist_id, status="available")
        
        assert len(gifts) == 1
        assert gifts[0].status == "available"


class TestWishlistService:
    """Тесты для WishlistService.regenerate_unique_link()"""
    
    def test_regenerate_link_success(self, db_session, test_user, test_wishlist):
        """Тест 4.1: Генерация новой ссылки для публичного вишлиста"""
        service = WishlistService(db_session)
        
        wishlist = service.regenerate_unique_link(
            test_wishlist.wishlist_id,
            test_user.user_id
        )
        
        assert wishlist.unique_link is not None
        assert len(wishlist.unique_link) > 0
    
    def test_regenerate_link_private_wishlist(self, db_session, test_user):
        """Тест 4.2: Ошибка при генерации ссылки для приватного списка"""
        service = WishlistService(db_session)
        wishlist_data = WishlistCreate(
            user_id=test_user.user_id,
            name="Private List",
            event_date="2025-12-31",
            is_private=True
        )
        private_wishlist = service.create_for_user(test_user.user_id, wishlist_data)
        
        with pytest.raises(ValueError, match="Cannot set unique link for private wishlist"):
            service.regenerate_unique_link(private_wishlist.wishlist_id, test_user.user_id)


class TestReservationService:
    """Тесты для ReservationService.reserve_gift()"""
    
    def test_reserve_gift_success(self, db_session, test_user, test_gift):
        """Тест 5.1: Успешное резервирование подарка"""
        from app.schemas.user import UserCreate
        service_user = UserService(db_session)
        another_user = service_user.register(UserCreate(
            login="anotheruser",
            email="another@test.com",
            password="pass12555553"
        ))
        
        service = ReservationService(db_session)
        reservation = service.reserve_gift(another_user.user_id, test_gift.gift_id)
        
        assert reservation.user_id == another_user.user_id
        assert reservation.gift_id == test_gift.gift_id
    
    def test_reserve_own_gift_error(self, db_session, test_user, test_gift):
        """Тест 5.2: Ошибка при попытке зарезервировать свой подарок"""
        service = ReservationService(db_session)
        
        with pytest.raises(ValueError, match="Cannot reserve your own gift"):
            service.reserve_gift(test_user.user_id, test_gift.gift_id)
    
    def test_reserve_nonexistent_gift(self, db_session, test_user):
        """Тест 5.3: Ошибка при резервировании несуществующего подарка"""
        service = ReservationService(db_session)
        
        with pytest.raises(ValueError, match="Gift not found"):
          service.reserve_gift(test_user.user_id, 99999)