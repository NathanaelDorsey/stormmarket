package com.nathanaeldorsey.stormmarket.common.exception;

/**
 * Successful result carrying a value.
 *
 * @param <T>   the value type
 * @param <E>   the error type (unused on success, present for interface consistency)
 * @param value the successful result value
 */
public record Ok<T, E extends Throwable>(T value) implements Result<T, E> {}