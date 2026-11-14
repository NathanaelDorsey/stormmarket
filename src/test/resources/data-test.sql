-- Storm Market Test Data Seed
-- This file is used by H2 in-memory database for testing
-- Data is NOT persisted to production (H2 is in-memory only)
-- USERS are created via API registration in bootstrap script (uses Argon2PasswordEncoder)
-- =========================================================
-- CATEGORIES
-- =========================================================
-- Electronics category
INSERT INTO categories (name, description, parent_id)
VALUES ('Electronics', 'Electronic devices and accessories', NULL);

-- Clothing category
INSERT INTO categories (name, description, parent_id)
VALUES ('Clothing', 'Apparel and fashion items', NULL);

-- Books category
INSERT INTO categories (name, description, parent_id)
VALUES ('Books', 'Books and publications', NULL);

-- =========================================================
-- PRODUCTS
-- =========================================================
-- Smartphone (Electronics)
INSERT INTO products (name, price, description, category_id, stock_quantity, status)
VALUES ('Smartphone X', 699.99, 'Latest smartphone with 128GB storage', 1, 50, 'ACTIVE');

-- Laptop Pro (Electronics)
INSERT INTO products (name, price, description, category_id, stock_quantity, status)
VALUES ('Laptop Pro', 1299.99, 'Professional laptop with 16GB RAM', 1, 30, 'ACTIVE');

-- T-Shirt (Clothing)
INSERT INTO products (name, price, description, category_id, stock_quantity, status)
VALUES ('Cotton T-Shirt', 19.99, '100% cotton, available in sizes S-XXL', 2, 100, 'ACTIVE');

-- Jeans (Clothing)
INSERT INTO products (name, price, description, category_id, stock_quantity, status)
VALUES ('Denim Jeans', 49.99, 'Classic fit denim jeans', 2, 75, 'ACTIVE');

-- Fiction Book (Books)
INSERT INTO products (name, price, description, category_id, stock_quantity, status)
VALUES ('The Great Novel', 14.99, 'A bestselling fiction book', 3, 200, 'ACTIVE');

-- Science Book (Books)
INSERT INTO products (name, price, description, category_id, stock_quantity, status)
VALUES ('Physics Handbook', 85.00, 'University level physics reference', 3, 25, 'ACTIVE');