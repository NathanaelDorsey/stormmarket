ALTER TABLE orders
    ADD COLUMN subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN shipping_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN currency_code VARCHAR(3) NOT NULL DEFAULT 'USD',
    ADD COLUMN shipping_address_id BIGINT NULL,
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_shipping_address
        FOREIGN KEY (shipping_address_id) REFERENCES shipping_addresses(id);