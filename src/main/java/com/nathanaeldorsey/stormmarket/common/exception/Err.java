package com.nathanaeldorsey.stormmarket.common.exception;

/**
 * Failed result carrying an expected error/exception.
 *
 * @param <T>   the success value type (unused on failure, present for interface consistency)
 * @param <E>   the error type
 * @param error the exception describing the failure
 */
public record Err<T, E extends Throwable>(E error) implements Result<T, E> {}