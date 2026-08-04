package dev.jceballos.stockpile.inventory.infrastructure.persistence;

import dev.jceballos.stockpile.infrastructure.persistence.SchemaInitializer;
import dev.jceballos.stockpile.infrastructure.persistence.SqliteConnectionFactory;
import dev.jceballos.stockpile.inventory.application.port.ProductQuery;
import dev.jceballos.stockpile.inventory.application.port.ProductView;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.PagedResult;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class SqliteProductReadRepositoryTest {

    private Connection connection;
    private SqliteProductWriteRepository writeRepository;
    private SqliteProductReadRepository readRepository;

    @BeforeEach
    void setUp() {
        connection = new SqliteConnectionFactory("jdbc:sqlite::memory:").createConnection();
        new SchemaInitializer().initialize(connection);
        writeRepository = new SqliteProductWriteRepository(connection);
        readRepository = new SqliteProductReadRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {
        assertThat(readRepository.findById(new ProductId("SKU-UNKNOWN"))).isEmpty();
    }

    @Test
    void shouldFindProductById() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        writeRepository.save(Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5));

        ProductView view = readRepository.findById(productId).orElseThrow();

        assertThat(view.name()).isEqualTo("Laptop");
        assertThat(view.stock()).isEqualTo(5);
    }

    @Test
    void shouldFilterByNameContains() {
        writeRepository.save(Product.register(new ProductId("SKU-1"), "Laptop 14", Money.of(new BigDecimal("999.00"), "USD"), 5));
        writeRepository.save(Product.register(new ProductId("SKU-2"), "Laptop 16", Money.of(new BigDecimal("1200.00"), "USD"), 3));
        writeRepository.save(Product.register(new ProductId("SKU-3"), "Mouse", Money.of(new BigDecimal("25.00"), "USD"), 10));

        PagedResult<ProductView> result = readRepository.findAll(ProductQuery.byNameContains("Laptop", 0, 10));

        assertThat(result.items()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    void shouldPaginateResults() {
        for (int i = 0; i < 5; i++) {
            writeRepository.save(Product.register(
                    new ProductId("SKU-" + i), "Product " + i, Money.of(new BigDecimal("10.00"), "USD"), 1));
        }

        PagedResult<ProductView> firstPage = readRepository.findAll(ProductQuery.firstPage(2));

        assertThat(firstPage.items()).hasSize(2);
        assertThat(firstPage.totalElements()).isEqualTo(5);
        assertThat(firstPage.totalPages()).isEqualTo(3);
    }
}