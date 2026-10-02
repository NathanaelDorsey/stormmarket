CREATE TABLE order_items(
                id BIGINT AUTO_INCREMENT PRIMARY KEY ,
                order_id BIGINT NOT NULL ,
                product_id BIGINT NOT NULL ,
                quantity INT NOT NULL CHECK (quantity>=0) ,
                subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0) ,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE ,
                FOREIGN KEY (product_id) REFERENCES products(id)
);