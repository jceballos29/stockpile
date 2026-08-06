package dev.jceballos.stockpile.infrastructure.persistence;

import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.inventory.infrastructure.persistence.SqliteProductWriteRepository;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.order.infrastructure.persistence.SqliteOrderWriteRepository;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;
import dev.jceballos.stockpile.shared.ProductSalesView;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SqliteSalesReportRepositoryTest {

    private static final Currency USD = Currency.getInstance("USD");

    private Connection connection;
    private SqliteSalesReportRepository reportRepository;

    @BeforeEach
    void setUp() {
        connection = new SqliteConnectionFactory("jdbc:sqlite::memory:").createConnection();
        new SchemaInitializer().initialize(connection);
        reportRepository = new SqliteSalesReportRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void shouldReturnTopSellingProductsOrderedByQuantityDescending() {
        SqliteProductWriteRepository productRepository = new SqliteProductWriteRepository(connection);
        SqliteOrderWriteRepository orderRepository = new SqliteOrderWriteRepository(connection);

        ProductId laptop = new ProductId("SKU-LAPTOP");
        ProductId mouse = new ProductId("SKU-MOUSE");
        productRepository.save(Product.register(laptop, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 10));
        productRepository.save(Product.register(mouse, "Mouse", Money.of(new BigDecimal("25.00"), "USD"), 20));

        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(laptop, 2, Money.of(new BigDecimal("999.00"), "USD"), 10);
        order.addLine(mouse, 5, Money.of(new BigDecimal("25.00"), "USD"), 20);
        orderRepository.save(order);

        List<ProductSalesView> topSelling = reportRepository.topSellingProducts(10);

        assertThat(topSelling).hasSize(2);
        assertThat(topSelling.get(0).productId()).isEqualTo(mouse);
        assertThat(topSelling.get(0).quantitySold()).isEqualTo(5);
        assertThat(topSelling.get(1).productId()).isEqualTo(laptop);
    }
}