package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.common.exception.Result;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of custom stock operations returning Result.
 * Wraps JPA queries and converts int/void returns to Result.
 */
@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Result<Integer, StockError> reserveStock(Long productId, int quantity) {
        Query query = entityManager.createQuery("""
            UPDATE Product p
            SET p.reservedStock = p.reservedStock + :quantity,
                p.version = p.version + 1
            WHERE p.id = :productId
              AND p.stockQuantity >= p.reservedStock + :quantity
            """);
        query.setParameter("productId", productId);
        query.setParameter("quantity", quantity);
        int rowsUpdated = query.executeUpdate();
        if (rowsUpdated > 0) {
            return Result.ok(rowsUpdated);
        }
        return Result.err(StockError.INSUFFICIENT_STOCK);
    }

    @Override
    @Transactional
    public Result<Integer, StockError> deductStock(Long productId, int quantity) {
        Query query = entityManager.createQuery("""
            UPDATE Product p
            SET p.stockQuantity = p.stockQuantity - :quantity,
                p.version = p.version + 1
            WHERE p.id = :productId
              AND p.stockQuantity >= :quantity
            """);
        query.setParameter("productId", productId);
        query.setParameter("quantity", quantity);
        int rowsUpdated = query.executeUpdate();
        if (rowsUpdated > 0) {
            return Result.ok(rowsUpdated);
        }
        return Result.err(StockError.INSUFFICIENT_STOCK);
    }

    @Override
    @Transactional
    public Result<Integer, StockError> captureStock(Long productId, int quantity) {
        Query query = entityManager.createQuery("""
            UPDATE Product p
            SET p.stockQuantity = p.stockQuantity - :quantity,
                p.reservedStock = p.reservedStock - :quantity,
                p.version = p.version + 1
            WHERE p.id = :productId
              AND p.stockQuantity >= :quantity
              AND p.reservedStock >= :quantity
            """);
        query.setParameter("productId", productId);
        query.setParameter("quantity", quantity);
        int rowsUpdated = query.executeUpdate();
        if (rowsUpdated > 0) {
            return Result.ok(rowsUpdated);
        }
        // Check if product exists but insufficient stock/reserved
        Product product = entityManager.find(Product.class, productId);
        if (product == null) {
            return Result.err(StockError.PRODUCT_NOT_FOUND);
        }
        if (product.getReservedStock() > 0) {
            return Result.err(StockError.CAPTURE_FAILED);
        }
        return Result.err(StockError.INSUFFICIENT_STOCK);
    }

    @Override
    @Transactional
    public Result<Void, StockError> restoreStock(Long productId, int quantity) {
        Query query = entityManager.createQuery("""
            UPDATE Product p
            SET p.stockQuantity = p.stockQuantity + :quantity,
                p.version = p.version + 1
            WHERE p.id = :productId
            """);
        query.setParameter("productId", productId);
        query.setParameter("quantity", quantity);
        int rowsUpdated = query.executeUpdate();
        if (rowsUpdated > 0) {
            return Result.ok(null);
        }
        return Result.err(StockError.PRODUCT_NOT_FOUND);
    }

    @Override
    @Transactional
    public Result<Void, StockError> releaseReservedStock(Long productId, int quantity) {
        Query query = entityManager.createQuery("""
            UPDATE Product p
            SET p.reservedStock = p.reservedStock - :quantity,
                p.version = p.version + 1
            WHERE p.id = :productId
            """);
        query.setParameter("productId", productId);
        query.setParameter("quantity", quantity);
        int rowsUpdated = query.executeUpdate();
        if (rowsUpdated > 0) {
            return Result.ok(null);
        }
        return Result.err(StockError.PRODUCT_NOT_FOUND);
    }
}