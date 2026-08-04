package dev.jceballos.stockpile.order.infrastructure.integration;

import dev.jceballos.stockpile.inventory.application.command.ReserveStockCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.RestoreStockCommandHandler;
import dev.jceballos.stockpile.inventory.application.port.InMemoryProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.order.application.port.ProductNotFoundException;
import dev.jceballos.stockpile.order.domain.InsufficientStockException;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryStockReservationAdapterTest {

    private InMemoryProductWriteRepository productRepository;
    private InventoryStockReservationAdapter adapter;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductWriteRepository();
        adapter = new InventoryStockReservationAdapter(
                new ReserveStockCommandHandler(productRepository),
                new RestoreStockCommandHandler(productRepository));
    }

    @Test
    void shouldReserveStockThroughInventoryContext() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        productRepository.save(Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5));

        adapter.reserve(productId, 3);

        assertThat(productRepository.findById(productId).orElseThrow().stock()).isEqualTo(2);
    }

    @Test
    void shouldTranslateProductNotFoundExceptionToOrderVocabulary() {
        assertThatThrownBy(() -> adapter.reserve(new ProductId("SKU-UNKNOWN"), 1))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldTranslateInsufficientStockExceptionToOrderVocabulary() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        productRepository.save(Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 1));

        assertThatThrownBy(() -> adapter.reserve(productId, 5))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("solicitado=5")
                .hasMessageContaining("disponible=1");
    }

    @Test
    void shouldReleaseStockThroughInventoryContext() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        productRepository.save(Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 2));
        adapter.reserve(productId, 2);

        adapter.release(productId, 2);

        assertThat(productRepository.findById(productId).orElseThrow().stock()).isEqualTo(2);
    }
}