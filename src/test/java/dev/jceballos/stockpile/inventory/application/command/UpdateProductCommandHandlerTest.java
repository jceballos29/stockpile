package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.inventory.application.port.InMemoryProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.inventory.domain.ProductNotFoundException;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateProductCommandHandlerTest {

    private InMemoryProductWriteRepository repository;
    private UpdateProductCommandHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductWriteRepository();
        handler = new UpdateProductCommandHandler(repository);
    }

    @Test
    void shouldUpdateAndPersistProduct() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        repository.save(Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5));

        handler.handle(new UpdateProductCommand(
                productId, "Laptop Pro", "Con mas RAM", Money.of(new BigDecimal("1099.00"), "USD")));

        Product updated = repository.findById(productId).orElseThrow();
        assertThat(updated.name()).isEqualTo("Laptop Pro");
        assertThat(updated.price().amount()).isEqualByComparingTo("1099.00");
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new UpdateProductCommand(
                new ProductId("SKU-UNKNOWN"), "X", "Y", Money.of(new BigDecimal("1.00"), "USD"))))
                .isInstanceOf(ProductNotFoundException.class);
    }
}