package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterProductCommandTest {

    @Test
    void shouldRejectNegativeInitialStock() {
        assertThatThrownBy(() -> new RegisterProductCommand(
                new ProductId("SKU-001"), "Laptop", Money.of(new BigDecimal("999.00"), "USD"), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullProductId() {
        assertThatThrownBy(() -> new RegisterProductCommand(
                null, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5))
                .isInstanceOf(NullPointerException.class);
    }
}