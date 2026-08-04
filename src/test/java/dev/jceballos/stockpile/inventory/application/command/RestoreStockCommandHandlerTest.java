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

class RestoreStockCommandHandlerTest {

    private InMemoryProductWriteRepository repository;
    private RestoreStockCommandHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductWriteRepository();
        handler = new RestoreStockCommandHandler(repository);
    }

    @Test
    void shouldRestoreStockAndPersistTheChange() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        repository.save(Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 2));

        handler.handle(new RestoreStockCommand(productId, 3));

        Product persisted = repository.findById(productId).orElseThrow();
        assertThat(persisted.stock()).isEqualTo(5);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new RestoreStockCommand(new ProductId("SKU-UNKNOWN"), 1)))
                .isInstanceOf(ProductNotFoundException.class);
    }
}