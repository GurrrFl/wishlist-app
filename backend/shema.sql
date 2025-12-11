PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS logs;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS gifts;
DROP TABLE IF EXISTS wishlists;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    login       TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    email       TEXT UNIQUE NOT NULL
		created_at  TEXT NOT NULL 
);

CREATE INDEX idx_users_login ON users(login);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE wishlists (
    wishlist_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    name        TEXT NOT NULL,
    event_date  DATE NOT NULL,
    is_private  INTEGER DEFAULT 0,
    unique_link TEXT UNIQUE,
    created_at  TEXT NOT NULL ,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_wishlists_user_id ON wishlists(user_id);
CREATE INDEX idx_wishlists_unique_link ON wishlists(unique_link);

CREATE TABLE gifts (
    gift_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    wishlist_id INTEGER NOT NULL,
    name        TEXT NOT NULL,
    description TEXT,
    price       REAL,
    store_link  TEXT,
    status      TEXT DEFAULT 'available' CHECK (status IN ('available','reserved')),
    created_at  TEXT NOT NULL ,
    FOREIGN KEY (wishlist_id) REFERENCES wishlists(wishlist_id) ON DELETE CASCADE
);

CREATE INDEX idx_gifts_wishlist_id ON gifts(wishlist_id);
CREATE INDEX idx_gifts_status ON gifts(status);

CREATE TABLE reservations (
    reservation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER NOT NULL,
    gift_id        INTEGER NOT NULL,
    reserved_date TEXT NOT NULL ,
    cancelled_at TEXT NOT NULL ,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (gift_id) REFERENCES gifts(gift_id) ON DELETE CASCADE,
    UNIQUE(user_id, gift_id)
);

CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_gift_id ON reservations(gift_id);

CREATE TABLE logs (
    log_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id    INTEGER,
    action_type TEXT NOT NULL,
    details    TEXT,
    timestamp  TEXT NOT NULL ,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TRIGGER trigger_reserve_gift
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
    SELECT
        CASE
            WHEN (SELECT status FROM gifts WHERE gift_id = NEW.gift_id) != 'available'
            THEN RAISE(ABORT, 'Gift is already reserved')
        END;

    UPDATE gifts
    SET status = 'reserved'
    WHERE gift_id = NEW.gift_id;
END;

CREATE TRIGGER trigger_cancel_reservation
AFTER UPDATE OF cancelled_at ON reservations
FOR EACH ROW
WHEN NEW.cancelled_at IS NOT NULL AND OLD.cancelled_at IS NULL
BEGIN
    UPDATE gifts
    SET status = 'available'
    WHERE gift_id = OLD.gift_id;
END;

CREATE TRIGGER trigger_log_reservation
AFTER INSERT ON reservations
FOR EACH ROW
BEGIN
    INSERT INTO logs (user_id, action_type, details)
    VALUES (NEW.user_id, 'RESERVE_GIFT', 'Gift ID: ' || NEW.gift_id);
END;
