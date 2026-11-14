package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.common.exception.Result;

/**
 * Custom repository interface for stock operations returning Result.
 * Implemented by ProductRepositoryImpl.
 */
public interface ProductRepositoryCustom {

    Result<Integer, StockError> reserveStock(Long productId, int quantity);

    Result<Integer, StockError> deductStock(Long productId, int quantity);

    Result<Integer, StockError> captureStock(Long productId, int quantity);

    Result<Void, StockError> restoreStock(Long productId, int quantity);

    Result<Void, StockError> releaseReservedStock(Long productId, int quantity);
}