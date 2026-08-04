package dev.jceballos.stockpile.order.infrastructure.persistence;

import dev.jceballos.stockpile.infrastructure.persistence.SchemaInitializer;
import dev.jceballos.stockpile.infrastructure.persistence.SqliteConnectionFactory;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.order.domain.OrderStatus;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class SqliteOrderWriteRepositoryTest {

    private static final Currency USD = Currency.getInstance("USD");

    private Connection connection;
    private SqliteOrderWriteRepository repository;

    @BeforeEach
    void setUp() {
        connection = new SqliteConnectionFactory("jdbc:sqlite::memory:").createConnection();
        new SchemaInitializer().initialize(connection);
        repository = new SqliteOrderWriteRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void shouldReturnEmptyWhenOrderDoesNotExist() {
        assertThat(repository.findById(OrderId.newId())).isEmpty();
    }

    @Test
    void shouldPersistAndRetrieveAnEmptyOpenOrder() {
        OrderId orderId = OrderId.newId();
        repository.save(Order.open(orderId, USD));

        Order retrieved = repository.findById(orderId).orElseThrow();

        assertThat(retrieved.orderId()).isEqualTo(orderId);
        assertThat(retrieved.status()).isEqualTo(OrderStatus.OPEN);
        assertThat(retrieved.lines()).isEmpty();
    }

    @Test
    void shouldPersistOrderLinesAndPreserveDecimalPrecision() {
        OrderId orderId = OrderId.newId();
        Order order = Order.open(orderId, USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 2, Money.of(new BigDecimal("999.00"), "USD"), 5);
        repository.save(order);

        Order retrieved = repository.findById(orderId).orElseThrow();

        assertThat(retrieved.lines()).hasSize(1);
        assertThat(retrieved.lines().get(0).quantity()).isEqualTo(2);
        assertThat(retrieved.calculateTotal().amount()).isEqualByComparingTo("1998.00");
    }

    @Test
    void shouldUpdateStatusOnSecondSaveWithoutDuplicatingTheOrder() {
        OrderId orderId = OrderId.newId();
        Order order = Order.open(orderId, USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        repository.save(order);

        order.pay();
        repository.save(order);

        Order retrieved = repository.findById(orderId).orElseThrow();
        assertThat(retrieved.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldReplaceLinesInsteadOfAccumulatingRowsOnSecondSave() {
        OrderId orderId = OrderId.newId();
        Order order = Order.open(orderId, USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        repository.save(order);

        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        repository.save(order);

        Order retrieved = repository.findById(orderId).orElseThrow();
        assertThat(retrieved.lines()).hasSize(1);
        assertThat(retrieved.lines().get(0).quantity()).isEqualTo(2);
    }
}