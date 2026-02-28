import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.database import Base
from app.schemas.user import UserCreate
from app.schemas.wishlist import WishlistCreate
from app.schemas.gift import GiftCreate, GiftUpdate
from app.services.user_service import UserService
from app.services.wishlist_service import WishlistService
from app.services.gift_service import GiftService


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
def another_user(db_session):
    """Создаёт второго пользователя для тестов прав доступа"""
    service = UserService(db_session)
    user_data = UserCreate(
        login="anotheruser",
        email="another@example.com",
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
def sample_gift_data(test_wishlist):
    """Базовые тестовые данные для создания подарка"""
    return GiftCreate(
        wishlist_id=test_wishlist.wishlist_id,
        name="Test Gift",
        description="A wonderful test gift",
        price=99.99,
        store_link="https://example.com/gift"
    )


@pytest.fixture
def created_gift(db_session, test_user, sample_gift_data):
    """Создаёт тестовый подарок"""
    service = GiftService(db_session)
    return service.create_for_user(test_user.user_id, sample_gift_data)


class TestGiftCreation:
    """Тесты создания подарков"""
    
    # ✅ ПОЗИТИВНЫЕ ТЕСТЫ
    
    def test_create_gift_with_all_fields(self, db_session, test_user, test_wishlist):
        """✅ Позитив: Создание подарка со всеми полями"""
        service = GiftService(db_session)
        gift_data = GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="PlayStation 5",
            description="Gaming console, white edition",
            price=499.99,
            store_link="https://store.example.com/ps5"
        )
        
        gift = service.create_for_user(test_user.user_id, gift_data)
        
        assert gift.gift_id is not None
        assert gift.name == "PlayStation 5"
        assert gift.description == "Gaming console, white edition"
        assert gift.price == 499.99
        assert gift.status == "available"
        assert gift.wishlist_id == test_wishlist.wishlist_id
        assert gift.created_at is not None
    
    def test_create_gift_with_minimal_fields(self, db_session, test_user, test_wishlist):
        """✅ Позитив: Создание подарка только с обязательными полями"""
        service = GiftService(db_session)
        gift_data = GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="Book"
        )
        
        gift = service.create_for_user(test_user.user_id, gift_data)
        
        assert gift.gift_id is not None
        assert gift.name == "Book"
        assert gift.description is None
        assert gift.price is None
        assert gift.store_link is None
        assert gift.status == "available"
    
    def test_create_gift_with_zero_price(self, db_session, test_user, test_wishlist):
        """✅ Позитив: Создание бесплатного подарка (цена = 0)"""
        service = GiftService(db_session)
        gift_data = GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="Free Sample",
            price=0.0
        )
        
        gift = service.create_for_user(test_user.user_id, gift_data)
        
        assert gift.price == 0.0
    
    def test_create_gift_with_long_name(self, db_session, test_user, test_wishlist):
        """✅ Позитив: Создание подарка с максимально длинным именем"""
        service = GiftService(db_session)
        long_name = "A" * 150  # максимум 150 символов
        gift_data = GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name=long_name
        )
        
        gift = service.create_for_user(test_user.user_id, gift_data)
        
        assert gift.name == long_name
    
    def test_create_multiple_gifts_in_wishlist(self, db_session, test_user, test_wishlist):
        """✅ Позитив: Создание нескольких подарков в одном вишлисте"""
        service = GiftService(db_session)
        
        gift1 = service.create_for_user(test_user.user_id, GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="Gift 1"
        ))
        gift2 = service.create_for_user(test_user.user_id, GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="Gift 2"
        ))
        
        assert gift1.gift_id != gift2.gift_id
        assert gift1.wishlist_id == gift2.wishlist_id
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ
    
    def test_create_gift_in_another_user_wishlist(self, db_session, test_user, another_user):
        """❌ Негатив: Попытка создать подарок в чужом вишлисте"""
        wishlist_service = WishlistService(db_session)
        another_wishlist = wishlist_service.create_for_user(
            another_user.user_id,
            WishlistCreate(
                user_id=another_user.user_id,
                name="Another's Wishlist",
                event_date="2025-12-31",
                is_private=False
            )
        )
        
        gift_service = GiftService(db_session)
        gift_data = GiftCreate(
            wishlist_id=another_wishlist.wishlist_id,
            name="Stolen Gift"
        )
        
        with pytest.raises(PermissionError, match="Access denied to wishlist"):
            gift_service.create_for_user(test_user.user_id, gift_data)
    
    def test_create_gift_with_empty_name(self, db_session, test_wishlist):
        """❌ Негатив: Создание подарка с пустым именем"""
        from pydantic import ValidationError
        
        with pytest.raises(ValidationError):
            GiftCreate(
                wishlist_id=test_wishlist.wishlist_id,
                name=""  # пустое имя
            )
    
    def test_create_gift_with_too_long_name(self, db_session, test_wishlist):
        """❌ Негатив: Создание подарка с именем длиннее 150 символов"""
        from pydantic import ValidationError
        
        with pytest.raises(ValidationError):
            GiftCreate(
                wishlist_id=test_wishlist.wishlist_id,
                name="A" * 151  # 151 символ
            )
    
    def test_create_gift_with_negative_price(self, db_session, test_wishlist):
        """❌ Негатив: Создание подарка с отрицательной ценой"""
        from pydantic import ValidationError
        
        with pytest.raises(ValidationError):
            GiftCreate(
                wishlist_id=test_wishlist.wishlist_id,
                name="Negative Gift",
                price=-10.0
            )
    
    def test_create_gift_with_invalid_url(self, db_session, test_wishlist):
        """❌ Негатив: Создание подарка с невалидной ссылкой"""
        from pydantic import ValidationError
        
        with pytest.raises(ValidationError):
            GiftCreate(
                wishlist_id=test_wishlist.wishlist_id,
                name="Gift",
                store_link="not-a-url"
            )
    
    def test_create_gift_in_nonexistent_wishlist(self, db_session, test_user):
        """❌ Негатив: Создание подарка в несуществующем вишлисте"""
        service = GiftService(db_session)
        gift_data = GiftCreate(
            wishlist_id=99999,
            name="Ghost Gift"
        )
        with pytest.raises(ValueError, match="Wishlist not found"):
            service.create_for_user(test_user.user_id, gift_data)
    
    def test_create_gift_with_too_long_description(self, db_session, test_wishlist):
        """❌ Негатив: Создание подарка с описанием длиннее 2000 символов"""
        from pydantic import ValidationError
        
        with pytest.raises(ValidationError):
            GiftCreate(
                wishlist_id=test_wishlist.wishlist_id,
                name="Gift",
                description="A" * 2001  # 2001 символ
            )


class TestGiftRetrieval:
    """Тесты получения подарков"""
    
    # ✅ ПОЗИТИВНЫЕ ТЕСТЫ
    
    def test_get_gift_by_id(self, db_session, test_user, created_gift):
        """✅ Позитив: Получение подарка по ID"""
        service = GiftService(db_session)
        
        gift = service.get_for_owner(created_gift.gift_id, test_user.user_id)
        
        assert gift.gift_id == created_gift.gift_id
        assert gift.name == created_gift.name
    
    def test_list_gifts_in_wishlist(self, db_session, test_user, test_wishlist):
        """✅ Позитив: Получение списка подарков вишлиста"""
        service = GiftService(db_session)
        
        # Создаём несколько подарков
        for i in range(3):
            service.create_for_user(test_user.user_id, GiftCreate(
                wishlist_id=test_wishlist.wishlist_id,
                name=f"Gift {i}"
            ))
        
        gifts = service.list_for_wishlist(
            owner_id=test_user.user_id,
            wishlist_id=test_wishlist.wishlist_id
        )
        
        assert len(gifts) == 3
        assert all(gift.wishlist_id == test_wishlist.wishlist_id for gift in gifts)
    
    def test_list_gifts_with_pagination(self, db_session, test_user, test_wishlist):
        """✅ Позитив: Пагинация списка подарков"""
        service = GiftService(db_session)
        
        # Создаём 5 подарков
        for i in range(5):
            service.create_for_user(test_user.user_id, GiftCreate(
                wishlist_id=test_wishlist.wishlist_id,
                name=f"Gift {i}"
            ))
        
        first_page = service.list_for_wishlist(
            owner_id=test_user.user_id,
            wishlist_id=test_wishlist.wishlist_id,
            offset=0,
            limit=2
        )
        second_page = service.list_for_wishlist(
            owner_id=test_user.user_id,
            wishlist_id=test_wishlist.wishlist_id,
            offset=2,
            limit=2
        )
        
        assert len(first_page) == 2
        assert len(second_page) == 2
        assert first_page[0].gift_id != second_page[0].gift_id
    
    def test_list_gifts_with_status_filter(self, db_session, test_user, test_wishlist):
        """✅ Позитив: Фильтрация подарков по статусу"""
        service = GiftService(db_session)
        
        # Создаём подарки с разными статусами
        gift1 = service.create_for_user(test_user.user_id, GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="Available Gift",
            status="available"
        ))
        gift2 = service.create_for_user(test_user.user_id, GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="Reserved Gift",
            status="reserved"
        ))
        
        available_gifts = service.list_for_wishlist(
            owner_id=test_user.user_id,
            wishlist_id=test_wishlist.wishlist_id,
            status="available"
        )
        
        assert len(available_gifts) == 1
        assert available_gifts[0].status == "available"
    
    def test_list_gifts_with_search(self, db_session, test_user, test_wishlist):
        """✅ Позитив: Поиск подарков по имени"""
        service = GiftService(db_session)
        
        service.create_for_user(test_user.user_id, GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="iPhone 15"
        ))
        service.create_for_user(test_user.user_id, GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="iPad Pro"
        ))
        service.create_for_user(test_user.user_id, GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="Samsung Galaxy"
        ))
        
        results = service.list_for_wishlist(
            owner_id=test_user.user_id,
            wishlist_id=test_wishlist.wishlist_id,
            search="iphone"
        )
        
        assert len(results) == 1
        assert "iPhone" in results[0].name
    
    def test_list_gifts_empty_wishlist(self, db_session, test_user, test_wishlist):
        """✅ Позитив: Получение списка подарков пустого вишлиста"""
        service = GiftService(db_session)
        
        gifts = service.list_for_wishlist(
            owner_id=test_user.user_id,
            wishlist_id=test_wishlist.wishlist_id
        )
        
        assert gifts == []
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ
    
    def test_get_nonexistent_gift(self, db_session, test_user):
        """❌ Негатив: Получение несуществующего подарка"""
        service = GiftService(db_session)
        with pytest.raises(ValueError, match="Gift not found"):
            service.get_for_owner(99999, test_user.user_id)
    
    def test_get_gift_from_another_user_wishlist(self, db_session, test_user, another_user, created_gift):
        """❌ Негатив: Попытка получить подарок из чужого вишлиста"""
        service = GiftService(db_session)
        
        with pytest.raises(PermissionError, match="Access denied to wishlist"):
            service.get_for_owner(created_gift.gift_id, another_user.user_id)
    
    def test_list_gifts_from_another_user_wishlist(self, db_session, test_user, another_user, test_wishlist):
        """❌ Негатив: Попытка получить список подарков чужого вишлиста"""
        service = GiftService(db_session)
        
        with pytest.raises(PermissionError):
            service.list_for_wishlist(
                owner_id=another_user.user_id,
                wishlist_id=test_wishlist.wishlist_id
            )


class TestGiftUpdate:
    """Тесты обновления подарков"""
    
    # ✅ ПОЗИТИВНЫЕ ТЕСТЫ
    
    def test_update_gift_name(self, db_session, test_user, created_gift):
        """✅ Позитив: Обновление имени подарка"""
        service = GiftService(db_session)
        update_data = GiftUpdate(name="Updated Gift Name")
        
        updated = service.update_for_owner(
            created_gift.gift_id,
            test_user.user_id,
            update_data
        )
        
        assert updated.name == "Updated Gift Name"
        assert updated.description == created_gift.description
    
    def test_update_gift_price(self, db_session, test_user, created_gift):
        """✅ Позитив: Обновление цены подарка"""
        service = GiftService(db_session)
        update_data = GiftUpdate(price=199.99)
        
        updated = service.update_for_owner(
            created_gift.gift_id,
            test_user.user_id,
            update_data
        )
        
        assert updated.price == 199.99
    
    def test_update_gift_description(self, db_session, test_user, created_gift):
        """✅ Позитив: Обновление описания подарка"""
        service = GiftService(db_session)
        update_data = GiftUpdate(description="New detailed description")
        
        updated = service.update_for_owner(
            created_gift.gift_id,
            test_user.user_id,
            update_data
        )
        
        assert updated.description == "New detailed description"
    
    def test_update_gift_store_link(self, db_session, test_user, created_gift):
        """✅ Позитив: Обновление ссылки на магазин"""
        service = GiftService(db_session)
        update_data = GiftUpdate(store_link="https://newstore.com/product")
        
        updated = service.update_for_owner(
            created_gift.gift_id,
            test_user.user_id,
            update_data
        )
        
        assert "newstore.com" in str(updated.store_link)
    
    def test_update_multiple_fields(self, db_session, test_user, created_gift):
        """✅ Позитив: Обновление нескольких полей одновременно"""
        service = GiftService(db_session)
        update_data = GiftUpdate(
            name="Super Gift",
            price=299.99,
            description="Amazing product"
        )
        
        updated = service.update_for_owner(
            created_gift.gift_id,
            test_user.user_id,
            update_data
        )
        
        assert updated.name == "Super Gift"
        assert updated.price == 299.99
        assert updated.description == "Amazing product"
    
    def test_move_gift_to_another_wishlist(self, db_session, test_user, created_gift):
        """✅ Позитив: Перенос подарка в другой вишлист того же пользователя"""
        wishlist_service = WishlistService(db_session)
        new_wishlist = wishlist_service.create_for_user(
            test_user.user_id,
            WishlistCreate(
                user_id=test_user.user_id,
                name="New Wishlist",
                event_date="2026-01-01",
                is_private=False
            )
        )
        
        gift_service = GiftService(db_session)
        update_data = GiftUpdate(wishlist_id=new_wishlist.wishlist_id)
        
        updated = gift_service.update_for_owner(
            created_gift.gift_id,
            test_user.user_id,
            update_data
        )
        
        assert updated.wishlist_id == new_wishlist.wishlist_id
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ
    
    def test_update_nonexistent_gift(self, db_session, test_user):
        """❌ Негатив: Обновление несуществующего подарка"""
        service = GiftService(db_session)
        update_data = GiftUpdate(name="Ghost Gift")
        with pytest.raises(ValueError, match="Gift not found"):
            service.update_for_owner(99999, test_user.user_id, update_data)
    
    def test_update_gift_from_another_user(self, db_session, test_user, another_user, created_gift):
        """❌ Негатив: Попытка обновить чужой подарок"""
        service = GiftService(db_session)
        update_data = GiftUpdate(name="Hacked Gift")
        
        with pytest.raises(PermissionError):
            service.update_for_owner(
                created_gift.gift_id,
                another_user.user_id,
                update_data
            )
    
    def test_update_gift_with_negative_price(self, db_session, test_user, created_gift):
        """❌ Негатив: Обновление цены на отрицательное значение"""
        from pydantic import ValidationError
        
        with pytest.raises(ValidationError):
            GiftUpdate(price=-50.0)
    
    def test_update_gift_with_invalid_url(self, db_session, test_user, created_gift):
        """❌ Негатив: Обновление ссылки на невалидный URL"""
        from pydantic import ValidationError
        
        with pytest.raises(ValidationError):
            GiftUpdate(store_link="invalid-url")
    
    def test_move_gift_to_another_user_wishlist(self, db_session, test_user, another_user, created_gift):
        """❌ Негатив: Попытка переместить подарок в чужой вишлист"""
        wishlist_service = WishlistService(db_session)
        another_wishlist = wishlist_service.create_for_user(
            another_user.user_id,
            WishlistCreate(
                user_id=another_user.user_id,
                name="Another's Wishlist",
                event_date="2026-01-01",
                is_private=False
            )
        )
        
        gift_service = GiftService(db_session)
        update_data = GiftUpdate(wishlist_id=another_wishlist.wishlist_id)
        
        with pytest.raises(PermissionError):
            gift_service.update_for_owner(
                created_gift.gift_id,
                test_user.user_id,
                update_data
            )


class TestGiftStatusManagement:
    """Тесты управления статусом подарка"""
    
    # ✅ ПОЗИТИВНЫЕ ТЕСТЫ
    
    def test_change_status_to_reserved(self, db_session, test_user, created_gift):
        """✅ Позитив: Смена статуса на 'reserved'"""
        service = GiftService(db_session)
        
        updated = service.change_status_for_owner(
            created_gift.gift_id,
            test_user.user_id,
            "reserved"
        )
        
        assert updated.status == "reserved"
    
    def test_change_status_to_available(self, db_session, test_user, created_gift):
        """✅ Позитив: Смена статуса на 'available'"""
        service = GiftService(db_session)
        
        # Сначала делаем reserved
        service.change_status_for_owner(
            created_gift.gift_id,
            test_user.user_id,
            "reserved"
        )
        
        # Потом обратно в available
        updated = service.change_status_for_owner(
            created_gift.gift_id,
            test_user.user_id,
            "available"
        )
        
        assert updated.status == "available"
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ
    
    def test_change_status_to_invalid_value(self, db_session, test_user, created_gift):
        """❌ Негатив: Смена статуса на невалидное значение"""
        service = GiftService(db_session)
        
        with pytest.raises(ValueError, match="Invalid gift status"):
            service.change_status_for_owner(
                created_gift.gift_id,
                test_user.user_id,
                "invalid_status"
            )
    
    def test_change_status_of_another_user_gift(self, db_session, test_user, another_user, created_gift):
        """❌ Негатив: Попытка изменить статус чужого подарка"""
        service = GiftService(db_session)
        
        with pytest.raises(PermissionError):
            service.change_status_for_owner(
                created_gift.gift_id,
                another_user.user_id,
                "reserved"
            )


class TestGiftDeletion:
    """Тесты удаления подарков"""
    
    # ✅ ПОЗИТИВНЫЕ ТЕСТЫ
    
    def test_delete_available_gift(self, db_session, test_user, created_gift):
        """✅ Позитив: Удаление доступного подарка"""
        service = GiftService(db_session)
        gift_id = created_gift.gift_id
        
        service.delete_for_owner(gift_id, test_user.user_id)
        
        # Проверяем, что подарок удалён
        from app.repositories.gift_repository import GiftRepository
        repo = GiftRepository(db_session)
        deleted = repo.get_by_id(gift_id)
        assert deleted is None
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ
    
    def test_delete_reserved_gift(self, db_session, test_user, created_gift):
        """❌ Негатив: Попытка удалить зарезервированный подарок"""
        service = GiftService(db_session)
        
        # Делаем подарок зарезервированным
        service.change_status_for_owner(
            created_gift.gift_id,
            test_user.user_id,
            "reserved"
        )
        
        with pytest.raises(ValueError, match="Cannot delete reserved gift"):
            service.delete_for_owner(created_gift.gift_id, test_user.user_id)
    
    def test_delete_nonexistent_gift(self, db_session, test_user):
        """❌ Негатив: Удаление несуществующего подарка"""
        service = GiftService(db_session)
        with pytest.raises(ValueError, match="Gift not found"):
            service.delete_for_owner(99999, test_user.user_id)
    
    def test_delete_another_user_gift(self, db_session, test_user, another_user, created_gift):
        """❌ Негатив: Попытка удалить чужой подарок"""
        service = GiftService(db_session)
        
        with pytest.raises(PermissionError):
            service.delete_for_owner(created_gift.gift_id, another_user.user_id)


class TestGiftValidation:
    """Тесты валидации данных подарка"""
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ (граничные значения)
    
    def test_gift_name_at_boundary_min(self, db_session, test_wishlist):
        """✅ Позитив: Имя подарка минимальной длины (1 символ)"""
        gift_data = GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="A"  # 1 символ - граница минимума
        )
        assert gift_data.name == "A"
    
    def test_gift_name_below_boundary_min(self, db_session, test_wishlist):
        """❌ Негатив: Имя подарка меньше минимума (0 символов)"""
        from pydantic import ValidationError
        
        with pytest.raises(ValidationError):
            GiftCreate(
                wishlist_id=test_wishlist.wishlist_id,
                name=""
            )
    
    def test_gift_price_at_boundary_min(self, db_session, test_wishlist):
        """✅ Позитив: Цена на границе минимума (0)"""
        gift_data = GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="Free Gift",
            price=0.0
        )
        assert gift_data.price == 0.0
    
    def test_gift_price_below_boundary_min(self, db_session, test_wishlist):
        """❌ Негатив: Цена ниже минимума (-0.01)"""
        from pydantic import ValidationError
        
        with pytest.raises(ValidationError):
            GiftCreate(
                wishlist_id=test_wishlist.wishlist_id,
                name="Gift",
                price=-0.01
            )
    
    def test_gift_description_at_max_length(self, db_session, test_wishlist):
        """✅ Позитив: Описание максимальной длины (2000 символов)"""
        max_description = "A" * 2000
        gift_data = GiftCreate(
            wishlist_id=test_wishlist.wishlist_id,
            name="Gift",
            description=max_description
        )
        assert len(gift_data.description) == 2000
    
    def test_gift_invalid_status_value(self, db_session, test_wishlist):
        """❌ Негатив: Невалидное значение статуса"""
        from pydantic import ValidationError
        
        with pytest.raises(ValidationError):
            GiftCreate(
                wishlist_id=test_wishlist.wishlist_id,
                name="Gift",
                status="pending"  # не в списке allowed values
            )
