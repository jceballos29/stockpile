package dev.jceballos.stockpile.inventory.domain;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void shouldRegisterProductWithGivenNameAndPriceAndStock() {
        Product product = Product.register(
                new ProductId("SKU-LAPTOP"), "Laptop 14\"", Money.of(new BigDecimal("999.00"), "USD"), 5);

        assertThat(product.productId()).isEqualTo(new ProductId("SKU-LAPTOP"));
        assertThat(product.name()).isEqualTo("Laptop 14\"");
        assertThat(product.price().amount()).isEqualByComparingTo("999.00");
        assertThat(product.stock()).isEqualTo(5);
    }

    @Test
    void shouldRejectNegativeInitialStock() {
        assertThatThrownBy(() -> Product.register(
                new ProductId("SKU-LAPTOP"), "Laptop", Money.of(new BigDecimal("999.00"), "USD"), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> Product.register(
                new ProductId("SKU-LAPTOP"), "   ", Money.of(new BigDecimal("999.00"), "USD"), 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullProductId() {
        assertThatThrownBy(() -> Product.register(
                null, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullPrice() {
        assertThatThrownBy(() -> Product.register(
                new ProductId("SKU-LAPTOP"), "Laptop", null, 5))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldBeEqualByIdentityRegardlessOfStock() {
        ProductId sameId = new ProductId("SKU-LAPTOP");
        Product first = Product.register(sameId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5);
        Product second = Product.register(sameId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 0);

        assertThat(first).isEqualTo(second);
    }
}