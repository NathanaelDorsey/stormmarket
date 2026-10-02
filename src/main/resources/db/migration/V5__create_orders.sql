CREATE TABLE orders (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                status VARCHAR(20) NOT NULL DEFAULT 'PLACED',
                ordered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                total_price DECIMAL(10,2) NOT NULL CHECK (total_price>= 0),
                FOREIGN KEY (user_id) REFERENCES users(id)

);