PRAGMA foreign_keys = ON;

-- Тестовые пользователи (пароль для всех: "password123")
INSERT INTO users (login, password_hash, email, created_at) VALUES 
('alice', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIxF3k0C1S', 'alice@example.com', datetime('now')),
('bob', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIxF3k0C1S', 'bob@example.com', datetime('now')),
('charlie', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIxF3k0C1S', 'charlie@example.com', datetime('now'));

-- Тестовые вишлисты
INSERT INTO wishlists (user_id, name, event_date, is_private, unique_link, created_at) VALUES
(1, 'Birthday 2026', '2026-03-15', 0, 'abc123def456', datetime('now')),
(1, 'New Year 2026', '2026-01-01', 1, NULL, datetime('now')),
(2, 'Wedding', '2026-06-20', 0, 'xyz789ghi012', datetime('now')),
(3, 'Christmas Wishlist', '2025-12-25', 0, 'christmas2025', datetime('now'));

-- Тестовые подарки
INSERT INTO gifts (wishlist_id, name, description, price, store_link, status, created_at) VALUES
(1, 'Clean Code Book', 'Robert Martin classic', 1500.00, 'https://example.com/book1', 'available', datetime('now')),
(1, 'Wireless Headphones', 'Bluetooth 5.0 with noise cancellation', 5000.00, 'https://example.com/headphones', 'available', datetime('now')),
(1, 'Mechanical Keyboard', 'Cherry MX switches', 8000.00, 'https://example.com/keyboard', 'available', datetime('now')),
(2, 'Board Game', 'Carcassonne or Ticket to Ride', 2500.00, NULL, 'available', datetime('now')),
(3, 'Coffee Machine', 'Capsule automatic', 12000.00, 'https://example.com/coffee', 'available', datetime('now')),
(3, 'Smart Watch', 'Fitness tracker', 15000.00, 'https://example.com/watch', 'available', datetime('now')),
(4, 'Winter Boots', 'Size 42, waterproof', 7000.00, NULL, 'available', datetime('now'));

-- Тестовая резервация (bob резервирует подарок alice)
INSERT INTO reservations (user_id, gift_id, reserved_date) VALUES
(2, 1, datetime('now'));

-- Проверка: gift_id=1 должен иметь status='reserved'
-- SELECT gift_id, name, status FROM gifts WHERE gift_id = 1;

-- Проверка: должна быть запись в логах
-- SELECT * FROM logs WHERE action_type = 'RESERVE_GIFT';
