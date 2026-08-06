package dev.jceballos.stockpile.order.infrastructure.integration;

import dev.jceballos.stockpile.inventory.application.port.InMemoryProductReadRepository;
import dev.jceballos.stockpile.inventory.application.port.ProductView;
import dev.jceballos.stockpile.inventory.application.query.ProductQueryHandler;
import dev.jceballos.stockpile.order.application.port.ProductNotFoundException;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryStockQueryAdapterTest {

    private InMemoryProductReadRepository productReadRepository;
    private InventoryStockQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        productReadRepository = new InMemoryProductReadRepository();
        adapter = new InventoryStockQueryAdapter(new ProductQueryHandler(productReadRepository));
    }

    @Test
    void shouldReturnAvailableStockThroughInventoryContext() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        productReadRepository.add(new ProductView(productId, "Laptop", "", Money.of(new BigDecimal("999.00"), "USD"), 5));

        int stock = adapter.availableStockOf(productId);

        assertThat(stock).isEqualTo(5);
    }

    @Test
    void shouldTranslateProductNotFoundExceptionToOrderVocabulary() {
        assertThatThrownBy(() -> adapter.availableStockOf(new ProductId("SKU-UNKNOWN")))
                .isInstanceOf(ProductNotFoundException.class);
    }
}