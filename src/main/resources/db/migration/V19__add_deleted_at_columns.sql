ALTER TABLE products
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE carts
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE users
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE categories
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE order_items
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE order_events
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE payments
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE coupons
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE shipping_addresses
    ADD COLUMN deleted_at TIMESTAMP NULL;