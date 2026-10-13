CREATE TABLE orders (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                created_at TIMESTAMP NOT NULL ,
                total DECIMAL(12,2) NOT NULL CHECK (total>= 0),
                FOREIGN KEY (user_id) REFERENCES users(id)

);