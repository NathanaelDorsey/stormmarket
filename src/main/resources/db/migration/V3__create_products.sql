CREATE TABLE products (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                description TEXT,
                price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
                stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
                image_URL VARCHAR(500)

);