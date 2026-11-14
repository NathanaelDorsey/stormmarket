package com.nathanaeldorsey.stormmarket.common.exception;

/**
 * Sealed interface representing the outcome of an operation that can either succeed or fail.
 *
 * Use {@link Ok} for successful results carrying a value, and {@link Err} for expected
 * failures carrying an exception. Callers must handle both cases, making expected failures
 * explicit at compile time rather than relying on unchecked exceptions for control flow.
 *
 * @param <T> the success value type
 * @param <E> the error type, must be a Throwable
 */
public sealed interface Result<T, E extends Throwable> permits Ok, Err {

    /**
     * Returns true if this result is a success (an {@link Ok}).
     */
    default boolean isOk() {
        return this instanceof Ok<?, ?>;
    }

    /**
     * Returns true if this result is a failure (an {@link Err}).
     */
    default boolean isErr() {
        return this instanceof Err<?, ?>;
    }

    /**
     * Creates a successful result.
     */
    static <T, E extends Throwable> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    /**
     * Creates a failed result.
     */
    static <T, E extends Throwable> Result<T, E> err(E error) {
        return new Err<>(error);
    }

    /**
     * Returns the error if this is an Err, otherwise throws IllegalStateException.
     */
    @SuppressWarnings("unchecked")
    default E getError() {
        if (this instanceof Err<?, ?> err) {
            return (E) err.error();
        }
        throw new IllegalStateException("Result is not an error");
    }

    /**
     * Returns the value if this is an Ok, otherwise throws IllegalStateException.
     */
    @SuppressWarnings("unchecked")
    default T getValue() {
        if (this instanceof Ok<?, ?> ok) {
            return (T) ok.value();
        }
        throw new IllegalStateException("Result is not a success");
    }
}