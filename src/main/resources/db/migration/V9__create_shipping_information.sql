CREATE TABLE shipping_information (
                id BIGINT AUTO_INCREMENT PRIMARY KEY ,
                order_id BIGINT NOT NULL UNIQUE ,
                shipping_method VARCHAR(255) NOT NULL ,
                shipping_address TEXT NOT NULL ,
                status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' ,
                FOREIGN KEY (order_id) REFERENCES orders(id)

);