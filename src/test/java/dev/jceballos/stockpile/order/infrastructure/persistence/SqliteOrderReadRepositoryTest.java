package dev.jceballos.stockpile.order.infrastructure.persistence;

import dev.jceballos.stockpile.infrastructure.persistence.SchemaInitializer;
import dev.jceballos.stockpile.infrastructure.persistence.SqliteConnectionFactory;
import dev.jceballos.stockpile.order.application.port.OrderQuery;
import dev.jceballos.stockpile.order.application.port.OrderView;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.order.domain.OrderStatus;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.PagedResult;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class SqliteOrderReadRepositoryTest {

    private static final Currency USD = Currency.getInstance("USD");

    private Connection connection;
    private SqliteOrderWriteRepository writeRepository;
    private SqliteOrderReadRepository readRepository;

    @BeforeEach
    void setUp() {
        connection = new SqliteConnectionFactory("jdbc:sqlite::memory:").createConnection();
        new SchemaInitializer().initialize(connection);
        writeRepository = new SqliteOrderWriteRepository(connection);
        readRepository = new SqliteOrderReadRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void shouldReturnEmptyWhenOrderDoesNotExist() {
        assertThat(readRepository.findById(OrderId.newId())).isEmpty();
    }

    @Test
    void shouldFindOrderWithLinesAndCalculatedTotal() {
        OrderId orderId = OrderId.newId();
        Order order = Order.open(orderId, USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 2, Money.of(new BigDecimal("999.00"), "USD"), 5);
        order.addLine(new ProductId("SKU-MOUSE"), 3, Money.of(new BigDecimal("25.00"), "USD"), 10);
        writeRepository.save(order);

        OrderView view = readRepository.findById(orderId).orElseThrow();

        assertThat(view.status()).isEqualTo(OrderStatus.OPEN);
        assertThat(view.lines()).hasSize(2);
        assertThat(view.total().amount()).isEqualByComparingTo("2073.00");
    }

    @Test
    void shouldFindEmptyOrderWithZeroTotal() {
        OrderId orderId = OrderId.newId();
        writeRepository.save(Order.open(orderId, USD));

        OrderView view = readRepository.findById(orderId).orElseThrow();

        assertThat(view.lines()).isEmpty();
        assertThat(view.total().amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldFilterByStatus() {
        OrderId openId = OrderId.newId();
        writeRepository.save(Order.open(openId, USD));

        OrderId paidId = OrderId.newId();
        Order paidOrder = Order.open(paidId, USD);
        paidOrder.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        paidOrder.pay();
        writeRepository.save(paidOrder);

        PagedResult<OrderView> result = readRepository.findAll(OrderQuery.byStatus(OrderStatus.PAID, 0, 10));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).orderId()).isEqualTo(paidId);
    }

    @Test
    void shouldPaginateResults() {
        for (int i = 0; i < 5; i++) {
            writeRepository.save(Order.open(OrderId.newId(), USD));
        }

        PagedResult<OrderView> firstPage = readRepository.findAll(OrderQuery.firstPage(2));

        assertThat(firstPage.items()).hasSize(2);
        assertThat(firstPage.totalElements()).isEqualTo(5);
        assertThat(firstPage.totalPages()).isEqualTo(3);
    }
}