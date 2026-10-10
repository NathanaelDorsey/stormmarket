CREATE TABLE cart_items(
                id BIGINT AUTO_INCREMENT PRIMARY KEY ,
                cart_id BIGINT NOT NULL ,
                product_id BIGINT NOT NULL ,
                quantity INT NOT NULL DEFAULT 1 CHECK (quantity>0) ,
                CONSTRAINT uq_cart_product UNIQUE (cart_id,product_id),
                FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE ,
                FOREIGN KEY (product_id) REFERENCES products(id)
)