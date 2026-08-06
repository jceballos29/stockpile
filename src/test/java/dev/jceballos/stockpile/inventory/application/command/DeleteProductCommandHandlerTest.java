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

class DeleteProductCommandHandlerTest {

    private InMemoryProductWriteRepository repository;
    private DeleteProductCommandHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductWriteRepository();
        handler = new DeleteProductCommandHandler(repository);
    }

    @Test
    void shouldDeleteProduct() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        repository.save(Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5));

        handler.handle(new DeleteProductCommand(productId));

        assertThat(repository.findById(productId)).isEmpty();
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new DeleteProductCommand(new ProductId("SKU-UNKNOWN"))))
                .isInstanceOf(ProductNotFoundException.class);
    }
}