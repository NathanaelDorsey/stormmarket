CREATE TABLE order_items(
                id BIGINT AUTO_INCREMENT PRIMARY KEY ,
                order_id BIGINT NOT NULL ,
                product_id BIGINT NOT NULL ,
                product_name VARCHAR(255) NOT NULL ,
                quantity INT NOT NULL CHECK (quantity>0) ,
                price DECIMAL(12,2) NOT NULL CHECK (price >= 0) ,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE ,
                FOREIGN KEY (product_id) REFERENCES products(id)
);