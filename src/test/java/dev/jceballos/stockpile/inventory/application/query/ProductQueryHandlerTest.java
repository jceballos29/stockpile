package dev.jceballos.stockpile.inventory.application.query;

import dev.jceballos.stockpile.inventory.application.port.InMemoryProductReadRepository;
import dev.jceballos.stockpile.inventory.application.port.ProductQuery;
import dev.jceballos.stockpile.inventory.application.port.ProductView;
import dev.jceballos.stockpile.inventory.domain.ProductNotFoundException;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.PagedResult;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductQueryHandlerTest {

    private InMemoryProductReadRepository readRepository;
    private ProductQueryHandler handler;

    @BeforeEach
    void setUp() {
        readRepository = new InMemoryProductReadRepository();
        handler = new ProductQueryHandler(readRepository);
    }

    @Test
    void shouldReturnProductViewById() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        readRepository.add(viewOf(productId, "Laptop"));

        ProductView result = handler.getById(productId);

        assertThat(result.productId()).isEqualTo(productId);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        assertThatThrownBy(() -> handler.getById(new ProductId("SKU-UNKNOWN")))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldFilterByNameContains() {
        readRepository.add(viewOf(new ProductId("SKU-1"), "Laptop 14"));
        readRepository.add(viewOf(new ProductId("SKU-2"), "Laptop 16"));
        readRepository.add(viewOf(new ProductId("SKU-3"), "Mouse"));

        PagedResult<ProductView> result = handler.list(ProductQuery.byNameContains("Laptop", 0, 10));

        assertThat(result.items()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    void shouldPaginateResults() {
        for (int i = 0; i < 5; i++) {
            readRepository.add(viewOf(new ProductId("SKU-" + i), "Product " + i));
        }

        PagedResult<ProductView> firstPage = handler.list(ProductQuery.firstPage(2));

        assertThat(firstPage.items()).hasSize(2);
        assertThat(firstPage.totalPages()).isEqualTo(3);
        assertThat(firstPage.hasNext()).isTrue();
    }

    private ProductView viewOf(ProductId productId, String name) {
        return new ProductView(productId, name, Money.of(new BigDecimal("10.00"), "USD"), 5);
    }
}