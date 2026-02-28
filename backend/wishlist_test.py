import pytest
from datetime import date
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.database import Base
from app.schemas.user import UserCreate
from app.schemas.wishlist import WishlistCreate, WishlistUpdate
from app.services.user_service import UserService
from app.services.wishlist_service import WishlistService

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
def user_service(db_session):
    return UserService(db_session)


@pytest.fixture
def wishlist_service(db_session):
    return WishlistService(db_session)


@pytest.fixture
def test_user(db_session, user_service):
    """Базовый пользователь-владелец вишлистов"""
    user_data = UserCreate(
        login="owner",
        email="owner@example.com",
        password="password123",
    )
    return user_service.register(user_data)


@pytest.fixture
def another_user(db_session, user_service):
    """Второй пользователь для негативных тестов прав доступа"""
    user_data = UserCreate(
        login="other",
        email="other@example.com",
        password="password123",
    )
    return user_service.register(user_data)


@pytest.fixture
def public_wishlist(db_session, wishlist_service, test_user):
    """Публичный вишлист с уникальной ссылкой"""
    data = WishlistCreate(
        user_id=test_user.user_id,
        name="Public list",
        event_date=date(2026, 1, 1),
        is_private=False,
        unique_link=None,
    )
    wl = wishlist_service.create_for_user(
        user_id=test_user.user_id,
        data=data,
        generate_link_if_missing=True,
    )
    return wl


@pytest.fixture
def private_wishlist(db_session, wishlist_service, test_user):
    """Приватный вишлист без ссылки"""
    data = WishlistCreate(
        user_id=test_user.user_id,
        name="Private list",
        event_date=date(2026, 1, 1),
        is_private=True,
        unique_link=None,
    )
    wl = wishlist_service.create_for_user(
        user_id=test_user.user_id,
        data=data,
        generate_link_if_missing=True,
    )
    return wl


class TestCreateWishlist:
    """Тесты создания вишлистов"""

    def test_create_public_wishlist_success(self, db_session, wishlist_service, test_user):
        """✅ Позитив: создание публичного вишлиста, генерируется unique_link"""
        data = WishlistCreate(
            user_id=test_user.user_id,
            name="Birthday",
            event_date=date(2026, 3, 15),
            is_private=False,
            unique_link=None,
        )

        wl = wishlist_service.create_for_user(
            user_id=test_user.user_id,
            data=data,
            generate_link_if_missing=True,
        )

        assert wl.wishlist_id is not None
        assert wl.user_id == test_user.user_id
        assert wl.name == "Birthday"
        assert wl.is_private == 0
        assert wl.unique_link is not None
        assert len(wl.unique_link) > 0

    def test_create_private_wishlist_without_link(self, db_session, wishlist_service, test_user):
        """✅ Позитив: приватный вишлист создаётся без unique_link"""
        data = WishlistCreate(
            user_id=test_user.user_id,
            name="Secret List",
            event_date=date(2026, 5, 10),
            is_private=True,
            unique_link=None,
        )

        wl = wishlist_service.create_for_user(
            user_id=test_user.user_id,
            data=data,
            generate_link_if_missing=True,
        )

        assert wl.is_private == 1
        assert wl.unique_link is None


class TestGetWishlist:
    """Тесты получения вишлистов"""

    def test_get_wishlist_for_owner_success(self, db_session, wishlist_service, public_wishlist, test_user):
        """✅ Позитив: владелец может получить свой вишлист по ID"""
        wl = wishlist_service.get_for_owner(
            wishlist_id=public_wishlist.wishlist_id,
            owner_id=test_user.user_id,
        )

        assert wl.wishlist_id == public_wishlist.wishlist_id
        assert wl.user_id == test_user.user_id

    def test_get_wishlist_for_wrong_owner_error(self, db_session, wishlist_service, public_wishlist, another_user):
        """❌ Негатив: чужой пользователь не может получить приватный вишлист по ID"""
        with pytest.raises(PermissionError):
            wishlist_service.get_for_owner(
                wishlist_id=public_wishlist.wishlist_id,
                owner_id=another_user.user_id,
            )


class TestListWishlists:
    """Тесты списка вишлистов пользователя"""

    def test_list_wishlists_for_user(self, db_session, wishlist_service, test_user, public_wishlist, private_wishlist):
        """✅ Позитив: список вишлистов пользователя (включая приватные)"""
        wishlists = wishlist_service.list_for_user(
            user_id=test_user.user_id,
            offset=0,
            limit=10,
            include_private=True,
        )

        assert len(wishlists) == 2
        ids = {wl.wishlist_id for wl in wishlists}
        assert public_wishlist.wishlist_id in ids
        assert private_wishlist.wishlist_id in ids

    def test_list_wishlists_public_only(self, db_session, wishlist_service, test_user, public_wishlist, private_wishlist):
        """✅ Позитив: список только публичных вишлистов"""
        wishlists = wishlist_service.list_for_user(
            user_id=test_user.user_id,
            offset=0,
            limit=10,
            include_private=False,
        )

        assert len(wishlists) == 1
        assert wishlists[0].wishlist_id == public_wishlist.wishlist_id
        assert wishlists[0].is_private == 0


class TestRegenerateUniqueLink:
    """Тесты регенерации уникальной ссылки"""

    def test_regenerate_link_success(self, db_session, wishlist_service, public_wishlist, test_user):
        """✅ Позитив: успешная регенерация ссылки для публичного списка"""
        old_link = public_wishlist.unique_link

        updated = wishlist_service.regenerate_unique_link(
            wishlist_id=public_wishlist.wishlist_id,
            owner_id=test_user.user_id,
        )

        assert updated.unique_link is not None
        assert updated.unique_link != old_link

    def test_regenerate_link_private_error(self, db_session, wishlist_service, private_wishlist, test_user):
        """❌ Негатив: ошибка при попытке сгенерировать ссылку для приватного списка"""
        with pytest.raises(ValueError, match="Cannot set unique link for private wishlist"):
            wishlist_service.regenerate_unique_link(
                wishlist_id=private_wishlist.wishlist_id,
                owner_id=test_user.user_id,
            )

    def test_regenerate_link_wrong_owner(self, db_session, wishlist_service, public_wishlist, another_user):
        """❌ Негатив: чужой пользователь не может менять ссылку"""
        with pytest.raises(PermissionError):
            wishlist_service.regenerate_unique_link(
                wishlist_id=public_wishlist.wishlist_id,
                owner_id=another_user.user_id,
            )


class TestUpdateWishlist:
    """Тесты обновления вишлиста"""

    def test_update_wishlist_name_success(self, db_session, wishlist_service, public_wishlist, test_user):
        """✅ Позитив: владелец может обновить название списка"""
        update = WishlistUpdate(name="Updated Name")

        updated = wishlist_service.update_for_user(
            wishlist_id=public_wishlist.wishlist_id,
            owner_id=test_user.user_id,
            data=update,
        )

        assert updated.name == "Updated Name"

    def test_update_wishlist_wrong_owner_error(self, db_session, wishlist_service, public_wishlist, another_user):
        """❌ Негатив: чужой пользователь не может обновить список"""
        update = WishlistUpdate(name="Hacked Name")

        with pytest.raises(PermissionError):
            wishlist_service.update_for_user(
                wishlist_id=public_wishlist.wishlist_id,
                owner_id=another_user.user_id,
                data=update,
            )


class TestDeleteWishlist:
    """Тесты удаления вишлиста"""

    def test_delete_wishlist_success(self, db_session, wishlist_service, public_wishlist, test_user):
        """✅ Позитив: владелец может удалить свой вишлист"""
        wl_id = public_wishlist.wishlist_id

        wishlist_service.delete_for_user(
            wishlist_id=wl_id,
            owner_id=test_user.user_id,
        )

        # Проверяем что список действительно удалён
        from app.repositories.wishlist_repository import WishlistRepository
        repo = WishlistRepository(db_session)
        deleted = repo.get_by_id(wl_id)
        assert deleted is None

    def test_delete_wishlist_wrong_owner_error(self, db_session, wishlist_service, public_wishlist, another_user):
        """❌ Негатив: чужой пользователь не может удалить список"""
        with pytest.raises(PermissionError):
            wishlist_service.delete_for_user(
                wishlist_id=public_wishlist.wishlist_id,
                owner_id=another_user.user_id,
            )
