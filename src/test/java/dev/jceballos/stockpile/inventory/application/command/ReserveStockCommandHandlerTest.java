package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.inventory.application.port.InMemoryProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.InsufficientStockException;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.inventory.domain.ProductNotFoundException;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReserveStockCommandHandlerTest {

    private InMemoryProductWriteRepository repository;
    private ReserveStockCommandHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductWriteRepository();
        handler = new ReserveStockCommandHandler(repository);
    }

    @Test
    void shouldReserveStockAndPersistTheChange() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        repository.save(Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5));

        handler.handle(new ReserveStockCommand(productId, 3));

        Product persisted = repository.findById(productId).orElseThrow();
        assertThat(persisted.stock()).isEqualTo(2);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new ReserveStockCommand(new ProductId("SKU-UNKNOWN"), 1)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldThrowWhenStockIsInsufficientAndNotPersistTheChange() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        repository.save(Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 1));

        assertThatThrownBy(() -> handler.handle(new ReserveStockCommand(productId, 5)))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(repository.findById(productId).orElseThrow().stock()).isEqualTo(1);
    }
}
