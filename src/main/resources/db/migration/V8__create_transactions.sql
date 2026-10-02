CREATE TABLE transactions(
                id BIGINT AUTO_INCREMENT PRIMARY KEY ,
                order_id BIGINT NOT NULL UNIQUE ,
                amount DECIMAL(10,2) NOT NULL CHECK (amount >=0) ,
                status VARCHAR(20) NOT NULL DEFAULT 'PENDING' ,
                payment_method VARCHAR(50) NOT NULL ,
                FOREIGN KEY (order_id) references orders(id)
);