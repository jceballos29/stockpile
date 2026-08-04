package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.inventory.application.port.InMemoryProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterProductCommandHandlerTest {

    private InMemoryProductWriteRepository repository;
    private RegisterProductCommandHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductWriteRepository();
        handler = new RegisterProductCommandHandler(repository);
    }

    @Test
    void shouldRegisterAndPersistProduct() {
        ProductId productId = new ProductId("SKU-LAPTOP");

        handler.handle(new RegisterProductCommand(
                productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5));

        Product persisted = repository.findById(productId).orElseThrow();
        assertThat(persisted.name()).isEqualTo("Laptop");
        assertThat(persisted.stock()).isEqualTo(5);
    }
}