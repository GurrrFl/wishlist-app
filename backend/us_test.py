import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.database import Base
from app.schemas.user import UserCreate, UserUpdate
from app.services.user_service import UserService


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
def sample_user_data():
    """Базовые тестовые данные для создания пользователя"""
    return UserCreate(
        login="testuser",
        email="test@example.com",
        password="securepass123"
    )


@pytest.fixture
def registered_user(db_session, sample_user_data):
    """Создаёт зарегистрированного пользователя для тестов"""
    service = UserService(db_session)
    return service.register(sample_user_data)


class TestUserRegistration:
    """Тесты регистрации пользователя"""
    
    # ✅ ПОЗИТИВНЫЕ ТЕСТЫ
    
    def test_register_with_valid_data(self, db_session):
        """✅ Позитив: Регистрация с валидными данными"""
        service = UserService(db_session)
        user_data = UserCreate(
            login="newuser",
            email="new@example.com",
            password="strongpass123"
        )
        
        user = service.register(user_data)
        
        assert user.user_id is not None
        assert user.login == "newuser"
        assert user.email == "new@example.com"
        assert user.password_hash != "strongpass123"  # пароль захеширован
        assert user.created_at is not None
    
    def test_register_with_min_length_credentials(self, db_session):
        """✅ Позитив: Регистрация с минимальной длиной полей"""
        service = UserService(db_session)
        user_data = UserCreate(
            login="abc",  # минимум 3 символа
            email="a@b.c",
            password="12345678"  # минимум 8 символов
        )
        
        user = service.register(user_data)
        
        assert user.user_id is not None
        assert user.login == "abc"
    
    def test_register_with_max_length_credentials(self, db_session):
        """✅ Позитив: Регистрация с максимальной длиной полей"""
        service = UserService(db_session)
        long_login = "a" * 50  # максимум 50 символов
        user_data = UserCreate(
            login=long_login,
            email="longname@example.com",
            password="x" * 128  # максимум 128 символов
        )
        
        user = service.register(user_data)
        
        assert user.user_id is not None
        assert user.login == long_login
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ
    
    def test_register_with_duplicate_login(self, db_session, registered_user):
        """❌ Негатив: Регистрация с уже существующим логином"""
        service = UserService(db_session)
        user_data = UserCreate(
            login=registered_user.login,
            email="different@example.com",
            password="password123"
        )
        
        with pytest.raises(ValueError, match="Login is already taken"):
            service.register(user_data)
    
    def test_register_with_duplicate_email(self, db_session, registered_user):
        """❌ Негатив: Регистрация с уже существующей почтой"""
        service = UserService(db_session)
        user_data = UserCreate(
            login="differentlogin",
            email=registered_user.email,
            password="password123"
        )
        
        with pytest.raises(ValueError, match="Email is already taken"):
            service.register(user_data)
    
    def test_register_with_short_login(self, db_session):
        """❌ Негатив: Регистрация с коротким логином (менее 3 символов)"""
        service = UserService(db_session)
        
        from pydantic import ValidationError
        with pytest.raises(ValidationError):
            UserCreate(
                login="ab",  # только 2 символа
                email="test@example.com",
                password="password123"
            )
    
    def test_register_with_short_password(self, db_session):
        """❌ Негатив: Регистрация с коротким паролем (менее 8 символов)"""
        service = UserService(db_session)
        
        from pydantic import ValidationError
        with pytest.raises(ValidationError):
            UserCreate(
                login="testuser",
                email="test@example.com",
                password="pass"  # только 4 символа
            )
    
    def test_register_with_invalid_email(self, db_session):
        """❌ Негатив: Регистрация с невалидной почтой"""
        service = UserService(db_session)
        
        from pydantic import ValidationError
        with pytest.raises(ValidationError):
            UserCreate(
                login="testuser",
                email="not-an-email",  # нет @
                password="password123"
            )
    
    def test_register_with_too_long_login(self, db_session):
        """❌ Негатив: Регистрация с логином длиннее 50 символов"""
        service = UserService(db_session)
        
        from pydantic import ValidationError
        with pytest.raises(ValidationError):
            UserCreate(
                login="a" * 51,  # 51 символ
                email="test@example.com",
                password="password123"
            )


class TestUserAuthentication:
    """Тесты аутентификации пользователя"""
    
    # ✅ ПОЗИТИВНЫЕ ТЕСТЫ
    
    def test_authenticate_with_login(self, db_session, registered_user):
        """✅ Позитив: Аутентификация по логину"""
        service = UserService(db_session)
        
        user = service.authenticate("testuser", "securepass123")
        
        assert user.user_id == registered_user.user_id
        assert user.login == registered_user.login
    
    def test_authenticate_with_email(self, db_session, registered_user):
        """✅ Позитив: Аутентификация по email"""
        service = UserService(db_session)
        
        user = service.authenticate("test@example.com", "securepass123")
        
        assert user.user_id == registered_user.user_id
        assert user.email == registered_user.email
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ
    
    def test_authenticate_with_wrong_password(self, db_session, registered_user):
        """❌ Негатив: Аутентификация с неверным паролем"""
        service = UserService(db_session)
        
        with pytest.raises(ValueError, match="Invalid credentials"):
            service.authenticate("testuser", "wrongpassword")
    
    def test_authenticate_nonexistent_user(self, db_session):
        """❌ Негатив: Аутентификация несуществующего пользователя"""
        service = UserService(db_session)
        
        with pytest.raises(ValueError, match="User not found"):
            service.authenticate("ghostuser", "anypassword123")
    
    def test_authenticate_with_empty_credentials(self, db_session):
        """❌ Негатив: Аутентификация с пустыми данными"""
        service = UserService(db_session)
        
        with pytest.raises(ValueError):
            service.authenticate("", "")


class TestUserRetrieval:
    """Тесты получения пользователя"""
    
    # ✅ ПОЗИТИВНЫЕ ТЕСТЫ
    
    def test_get_user_by_id(self, db_session, registered_user):
        """✅ Позитив: Получение пользователя по ID"""
        service = UserService(db_session)
        
        user = service.get_by_id(registered_user.user_id)
        
        assert user is not None
        assert user.user_id == registered_user.user_id
        assert user.login == registered_user.login
    
    def test_get_user_by_login(self, db_session, registered_user):
        """✅ Позитив: Получение пользователя по логину"""
        service = UserService(db_session)
        
        user = service.get_by_login(registered_user.login)
        
        assert user is not None
        assert user.user_id == registered_user.user_id
    
    def test_get_user_by_email(self, db_session, registered_user):
        """✅ Позитив: Получение пользователя по email"""
        service = UserService(db_session)
        
        user = service.get_by_email(registered_user.email)
        
        assert user is not None
        assert user.user_id == registered_user.user_id
    
    def test_list_users(self, db_session):
        """✅ Позитив: Получение списка пользователей"""
        service = UserService(db_session)
        
        # Создаём нескольких пользователей
        for i in range(3):
            service.register(UserCreate(
                login=f"user{i}",
                email=f"user{i}@example.com",
                password="password123"
            ))
        
        users = service.list_users(offset=0, limit=10)
        
        assert len(users) == 3
        assert all(user.user_id is not None for user in users)
    
    def test_list_users_with_pagination(self, db_session):
        """✅ Позитив: Пагинация списка пользователей"""
        service = UserService(db_session)
        
        # Создаём 5 пользователей
        for i in range(5):
            service.register(UserCreate(
                login=f"user{i}",
                email=f"user{i}@example.com",
                password="password123"
            ))
        
        first_page = service.list_users(offset=0, limit=2)
        second_page = service.list_users(offset=2, limit=2)
        
        assert len(first_page) == 2
        assert len(second_page) == 2
        assert first_page[0].user_id != second_page[0].user_id
    
    def test_list_users_with_search(self, db_session):
        """✅ Позитив: Поиск пользователей по подстроке"""
        service = UserService(db_session)
        
        service.register(UserCreate(login="alice", email="alice@example.com", password="password123"))
        service.register(UserCreate(login="bob", email="bob@example.com", password="password123"))
        service.register(UserCreate(login="alice2", email="alice2@example.com", password="password123"))
        
        results = service.list_users(search="alice")
        
        assert len(results) == 2
        assert all("alice" in user.login for user in results)
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ
    
    def test_get_user_by_nonexistent_id(self, db_session):
        """❌ Негатив: Получение несуществующего пользователя по ID"""
        service = UserService(db_session)
        
        user = service.get_by_id(99999)
        
        assert user is None
    
    def test_get_user_by_nonexistent_login(self, db_session):
        """❌ Негатив: Получение несуществующего пользователя по логину"""
        service = UserService(db_session)
        
        user = service.get_by_login("ghostuser")
        
        assert user is None
    
    def test_list_users_empty_database(self, db_session):
        """❌ Негатив: Список пользователей в пустой БД"""
        service = UserService(db_session)
        
        users = service.list_users()
        
        assert users == []


class TestUserUpdate:
    """Тесты обновления профиля пользователя"""
    
    # ✅ ПОЗИТИВНЫЕ ТЕСТЫ
    
    def test_update_login(self, db_session, registered_user):
        """✅ Позитив: Обновление логина"""
        service = UserService(db_session)
        update_data = UserUpdate(login="newlogin")
        
        updated_user = service.update_profile(registered_user.user_id, update_data)
        
        assert updated_user.login == "newlogin"
        assert updated_user.email == registered_user.email  # не изменилась
    
    def test_update_email(self, db_session, registered_user):
        """✅ Позитив: Обновление email"""
        service = UserService(db_session)
        update_data = UserUpdate(email="newemail@example.com")
        
        updated_user = service.update_profile(registered_user.user_id, update_data)
        
        assert updated_user.email == "newemail@example.com"
        assert updated_user.login == registered_user.login  # не изменился
    
    def test_update_password(self, db_session, registered_user):
        """✅ Позитив: Обновление пароля"""
        service = UserService(db_session)
        old_hash = registered_user.password_hash
        update_data = UserUpdate(password="newsecurepass123")
        
        updated_user = service.update_profile(registered_user.user_id, update_data)
        
        assert updated_user.password_hash != old_hash
        # Проверяем, что новый пароль работает
        authenticated = service.authenticate(updated_user.login, "newsecurepass123")
        assert authenticated.user_id == updated_user.user_id
    
    def test_update_multiple_fields(self, db_session, registered_user):
        """✅ Позитив: Обновление нескольких полей одновременно"""
        service = UserService(db_session)
        update_data = UserUpdate(
            login="updatedlogin",
            email="updated@example.com",
            password="updatedpass123"
        )
        
        updated_user = service.update_profile(registered_user.user_id, update_data)
        
        assert updated_user.login == "updatedlogin"
        assert updated_user.email == "updated@example.com"
        # Проверяем новый пароль
        authenticated = service.authenticate("updatedlogin", "updatedpass123")
        assert authenticated.user_id == updated_user.user_id
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ
    
    def test_update_nonexistent_user(self, db_session):
        """❌ Негатив: Обновление несуществующего пользователя"""
        service = UserService(db_session)
        update_data = UserUpdate(login="newlogin")
        
        with pytest.raises(ValueError, match="User not found"):
            service.update_profile(99999, update_data)
    
    def test_update_to_duplicate_login(self, db_session, registered_user):
        """❌ Негатив: Обновление логина на уже существующий"""
        service = UserService(db_session)
        
        # Создаём второго пользователя
        another_user = service.register(UserCreate(
            login="anotheruser",
            email="another@example.com",
            password="password123"
        ))
        
        # Пытаемся обновить логин первого пользователя на логин второго
        update_data = UserUpdate(login="anotheruser")
        
        with pytest.raises(ValueError, match="Login is already taken"):
            service.update_profile(registered_user.user_id, update_data)
    
    def test_update_to_duplicate_email(self, db_session, registered_user):
        """❌ Негатив: Обновление email на уже существующий"""
        service = UserService(db_session)
        
        # Создаём второго пользователя
        another_user = service.register(UserCreate(
            login="anotheruser",
            email="another@example.com",
            password="password123"
        ))
        
        # Пытаемся обновить email первого пользователя на email второго
        update_data = UserUpdate(email="another@example.com")
        
        with pytest.raises(ValueError, match="Email is already taken"):
            service.update_profile(registered_user.user_id, update_data)
    
    def test_update_with_short_password(self, db_session, registered_user):
        """❌ Негатив: Обновление на слишком короткий пароль"""
        service = UserService(db_session)
        
        from pydantic import ValidationError
        with pytest.raises(ValidationError):
            UserUpdate(password="short")  # только 5 символов


class TestUserDeletion:
    """Тесты удаления пользователя"""
    
    # ✅ ПОЗИТИВНЫЕ ТЕСТЫ
    
    def test_delete_user(self, db_session, registered_user):
        """✅ Позитив: Удаление существующего пользователя"""
        service = UserService(db_session)
        user_id = registered_user.user_id
        
        service.delete_user(user_id)
        
        # Проверяем, что пользователь действительно удалён
        deleted_user = service.get_by_id(user_id)
        assert deleted_user is None
    
    def test_delete_user_cascades_to_wishlists(self, db_session, registered_user):
        """✅ Позитив: Удаление пользователя удаляет его вишлисты (каскад)"""
        from app.services.wishlist_service import WishlistService
        from app.schemas.wishlist import WishlistCreate
        
        # Создаём вишлист для пользователя
        wishlist_service = WishlistService(db_session)
        wishlist_data = WishlistCreate(
            user_id=registered_user.user_id,
            name="Test Wishlist",
            event_date="2025-12-31",
            is_private=False
        )
        wishlist = wishlist_service.create_for_user(registered_user.user_id, wishlist_data)
        wishlist_id = wishlist.wishlist_id
        
        # Удаляем пользователя
        user_service = UserService(db_session)
        user_service.delete_user(registered_user.user_id)
        
        # Проверяем, что вишлист тоже удалён
        from app.repositories.wishlist_repository import WishlistRepository
        repo = WishlistRepository(db_session)
        deleted_wishlist = repo.get_by_id(wishlist_id)
        assert deleted_wishlist is None
    
    # ❌ НЕГАТИВНЫЕ ТЕСТЫ
    
    def test_delete_nonexistent_user(self, db_session):
        """❌ Негатив: Удаление несуществующего пользователя"""
        service = UserService(db_session)
        
        with pytest.raises(ValueError, match="User not found"):
            service.delete_user(99999)