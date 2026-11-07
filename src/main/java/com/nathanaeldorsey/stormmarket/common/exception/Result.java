package com.nathanaeldorsey.stormmarket.common.exception;

/**
 * Sealed interface representing the outcome of an operation that can either succeed or fail.
 *
 * Use {@link Ok} for successful results carrying a value, and {@link Err} for expected
 * failures carrying an exception. Callers must handle both cases, making expected failures
 * explicit at compile time rather than relying on unchecked exceptions for control flow.
 *
 * @param <T> the success value type
 * @param <E  the error type, must be a Throwable
 */
public sealed interface Result<T, E extends Throwable> permits Ok, Err {

    /**
     * Returns true if this result is a success (an {@link Ok}).
     */
    default boolean isOk() {
        return this instanceof Ok;
    }

    /**
     * Returns true if this result is a failure (an {@link Err}).
     */
    default boolean isErr() {
        return this instanceof Err;
    }
}

/**
 * Successful result carrying a value.
 *
 * @param <T>   the value type
 * @param <E>   the error type (unused on success, present for interface consistency)
 * @param value the successful result value
 */
record Ok<T, E extends Throwable>(T value) implements Result<T, E> {}

/**
 * Failed result carrying an expected error/exception.
 *
 * @param <T>   the success value type (unused on failure, present for interface consistency)
 * @param <E>   the error type
 * @param error the exception describing the failure
 */
record Err<T, E extends Throwable>(E error) implements Result<T, E> {}
