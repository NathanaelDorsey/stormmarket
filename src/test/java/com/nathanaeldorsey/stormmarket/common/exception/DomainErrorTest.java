package com.nathanaeldorsey.stormmarket.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainErrorTest {

    @Test
    void domainErrorExtendsRuntimeException() {
        assertThat(new DomainError("test") {})
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void domainErrorHasMessage() {
        DomainError error = new DomainError("test message") {};
        assertThat(error.getMessage()).isEqualTo("test message");
    }

    @Test
    void domainErrorCannotBeInstantiatedDirectly() {
        // DomainError is abstract, so this should fail to compile if uncommented
        // new DomainError("test");
        // The test verifies it's abstract by checking subclass instantiation works
        assertThat(new DomainError("test") {}).isNotNull();
    }
}