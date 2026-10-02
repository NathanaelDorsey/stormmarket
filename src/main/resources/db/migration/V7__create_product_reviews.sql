CREATE TABLE product_reviews (
                id BIGINT AUTO_INCREMENT PRIMARY KEY ,
                user_id BIGINT NOT NULL ,
                product_id BIGINT NOT NULL ,
                rating DECIMAL(10,2) NOT NULL CHECK (rating>=1.0 AND rating <=5.0) ,
                review_text TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE  
);