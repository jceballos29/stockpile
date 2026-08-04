package dev.jceballos.stockpile.inventory.infrastructure.persistence;

import dev.jceballos.stockpile.infrastructure.persistence.SchemaInitializer;
import dev.jceballos.stockpile.infrastructure.persistence.SqliteConnectionFactory;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class SqliteProductWriteRepositoryTest {

    private Connection connection;
    private SqliteProductWriteRepository repository;

    @BeforeEach
    void setUp() {
        connection = new SqliteConnectionFactory("jdbc:sqlite::memory:").createConnection();
        new SchemaInitializer().initialize(connection);
        repository = new SqliteProductWriteRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {
        assertThat(repository.findById(new ProductId("SKU-UNKNOWN"))).isEmpty();
    }

    @Test
    void shouldPersistAndRetrieveANewProduct() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        Product product = Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5);

        repository.save(product);

        Product retrieved = repository.findById(productId).orElseThrow();
        assertThat(retrieved.name()).isEqualTo("Laptop");
        assertThat(retrieved.price().amount()).isEqualByComparingTo("999.00");
        assertThat(retrieved.price().currency().getCurrencyCode()).isEqualTo("USD");
        assertThat(retrieved.stock()).isEqualTo(5);
    }

    @Test
    void shouldUpdateStockOnSecondSaveWithoutDuplicatingTheProduct() {
        ProductId productId = new ProductId("SKU-LAPTOP");
        Product product = Product.register(productId, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5);
        repository.save(product);

        product.reserve(2);
        repository.save(product);

        Product retrieved = repository.findById(productId).orElseThrow();
        assertThat(retrieved.stock()).isEqualTo(3);
    }
}