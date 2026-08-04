package dev.jceballos.stockpile.order.infrastructure.persistence;

import dev.jceballos.stockpile.infrastructure.persistence.PersistenceException;
import dev.jceballos.stockpile.order.application.port.OrderWriteRepository;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.order.domain.OrderLine;
import dev.jceballos.stockpile.order.domain.OrderStatus;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador SQLite de {@code OrderWriteRepository}. {@code save()} escribe
 * en dos tablas ({@code orders}, {@code order_lines}) pero NO maneja su
 * propia transacción: el límite transaccional le pertenece a quien orquesta
 * el caso de uso (ver {@code UnitOfWork}), no al repositorio. Los handlers
 * que llaman a este método (Pay, Dispatch, AddProductToOrder, Cancel)
 * son responsables de envolver la operación completa en
 * {@code unitOfWork.execute(...)}.
 */
public class SqliteOrderWriteRepository implements OrderWriteRepository {

    private final Connection connection;

    public SqliteOrderWriteRepository(Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public void save(Order order) {
        try {
            upsertOrderHeader(order);
            replaceOrderLines(order);
        } catch (SQLException e) {
            throw new PersistenceException("Error al guardar el pedido " + order.orderId(), e);
        }
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        String headerSql = "SELECT status, currency FROM orders WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(headerSql)) {
            statement.setString(1, orderId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                OrderStatus status = OrderStatus.valueOf(resultSet.getString("status"));
                Currency currency = Currency.getInstance(resultSet.getString("currency"));
                List<OrderLine> lines = findLines(orderId, currency);
                return Optional.of(Order.reconstitute(orderId, currency, status, lines));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar el pedido " + orderId, e);
        }
    }

    private void upsertOrderHeader(Order order) throws SQLException {
        String updateSql = "UPDATE orders SET status = ?, currency = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setString(1, order.status().name());
            statement.setString(2, order.currency().getCurrencyCode());
            statement.setString(3, order.orderId().toString());
            if (statement.executeUpdate() == 0) {
                insertOrderHeader(order);
            }
        }
    }

    private void insertOrderHeader(Order order) throws SQLException {
        String insertSql = "INSERT INTO orders (id, status, currency) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setString(1, order.orderId().toString());
            statement.setString(2, order.status().name());
            statement.setString(3, order.currency().getCurrencyCode());
            statement.executeUpdate();
        }
    }

    private void replaceOrderLines(Order order) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM order_lines WHERE order_id = ?")) {
            delete.setString(1, order.orderId().toString());
            delete.executeUpdate();
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO order_lines (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)")) {
            for (OrderLine line : order.lines()) {
                insert.setString(1, order.orderId().toString());
                insert.setString(2, line.productId().value());
                insert.setInt(3, line.quantity());
                insert.setString(4, line.unitPrice().amount().toPlainString());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private List<OrderLine> findLines(OrderId orderId, Currency currency) throws SQLException {
        List<OrderLine> lines = new ArrayList<>();
        String sql = "SELECT product_id, quantity, unit_price FROM order_lines WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ProductId productId = new ProductId(resultSet.getString("product_id"));
                    int quantity = resultSet.getInt("quantity");
                    Money unitPrice = new Money(new BigDecimal(resultSet.getString("unit_price")), currency);
                    lines.add(new OrderLine(productId, quantity, unitPrice));
                }
            }
        }
        return lines;
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Si el rollback mismo falla, la conexión probablemente ya quedo
            // inválida; no hay una recuperación mejor posible desde aca.
        }
    }

    private void restoreAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
            // Mismo caso que en rollbackQuietly.
        }
    }
}