package dev.jceballos.stockpile.order.domain;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderLineTest {

    @Test
    void shouldCalculateLineTotal() {
        OrderLine line = new OrderLine(
                new ProductId("SKU-001"),
                3,
                Money.of(new BigDecimal("10.00"), "USD"));

        assertThat(line.lineTotal().amount()).isEqualByComparingTo("30.00");
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThatThrownBy(() -> new OrderLine(
                new ProductId("SKU-001"), 0, Money.of(new BigDecimal("10.00"), "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThatThrownBy(() -> new OrderLine(
                new ProductId("SKU-001"), -1, Money.of(new BigDecimal("10.00"), "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullProductId() {
        assertThatThrownBy(() -> new OrderLine(
                null, 1, Money.of(new BigDecimal("10.00"), "USD")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullUnitPrice() {
        assertThatThrownBy(() -> new OrderLine(
                new ProductId("SKU-001"), 1, null))
                .isInstanceOf(NullPointerException.class);
    }
}
