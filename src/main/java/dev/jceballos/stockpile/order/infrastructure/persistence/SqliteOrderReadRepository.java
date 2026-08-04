package dev.jceballos.stockpile.order.infrastructure.persistence;

import dev.jceballos.stockpile.infrastructure.persistence.PersistenceException;
import dev.jceballos.stockpile.order.application.port.OrderLineView;
import dev.jceballos.stockpile.order.application.port.OrderQuery;
import dev.jceballos.stockpile.order.application.port.OrderReadRepository;
import dev.jceballos.stockpile.order.application.port.OrderView;
import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.order.domain.OrderStatus;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.PagedResult;
import dev.jceballos.stockpile.shared.ProductId;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador SQLite de {@code OrderReadRepository}. Usa {@code LEFT JOIN}
 * entre {@code orders} y {@code order_lines} para traer el pedido completo
 * (encabezado + lineas) en una sola consulta -- {@code LEFT}, no
 * {@code JOIN} a secas, porque un pedido sin lineas todavia debe aparecer
 * (con cero filas de {@code order_lines} asociadas), no desaparecer del
 * resultado.
 */
public class SqliteOrderReadRepository implements OrderReadRepository {

    private final Connection connection;

    public SqliteOrderReadRepository(Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public Optional<OrderView> findById(OrderId orderId) {
        String sql = """
                SELECT o.id AS order_id, o.status, o.currency,
                       ol.product_id, ol.quantity, ol.unit_price
                FROM orders o
                LEFT JOIN order_lines ol ON ol.order_id = o.id
                WHERE o.id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<OrderView> views = mapRows(resultSet);
                return views.isEmpty() ? Optional.empty() : Optional.of(views.get(0));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar el pedido " + orderId, e);
        }
    }

    @Override
    public PagedResult<OrderView> findAll(OrderQuery query) {
        try {
            long totalElements = countMatching(query);
            List<OrderView> items = findPage(query);
            return new PagedResult<>(items, query.page(), query.pageSize(), totalElements);
        } catch (SQLException e) {
            throw new PersistenceException("Error al listar pedidos", e);
        }
    }

    private long countMatching(OrderQuery query) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE (? IS NULL OR status = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String statusFilter = query.status().map(OrderStatus::name).orElse(null);
            statement.setString(1, statusFilter);
            statement.setString(2, statusFilter);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private List<OrderView> findPage(OrderQuery query) throws SQLException {
        String sql = """
                SELECT o.id AS order_id, o.status, o.currency,
                       ol.product_id, ol.quantity, ol.unit_price
                FROM orders o
                LEFT JOIN order_lines ol ON ol.order_id = o.id
                WHERE (? IS NULL OR o.status = ?)
                  AND o.id IN (
                      SELECT id FROM orders
                      WHERE (? IS NULL OR status = ?)
                      ORDER BY id
                      LIMIT ? OFFSET ?
                  )
                ORDER BY o.id
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String statusFilter = query.status().map(OrderStatus::name).orElse(null);
            statement.setString(1, statusFilter);
            statement.setString(2, statusFilter);
            statement.setString(3, statusFilter);
            statement.setString(4, statusFilter);
            statement.setInt(5, query.pageSize());
            statement.setInt(6, query.page() * query.pageSize());
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        }
    }

    private List<OrderView> mapRows(ResultSet resultSet) throws SQLException {
        Map<String, OrderId> orderIds = new LinkedHashMap<>();
        Map<String, OrderStatus> statuses = new LinkedHashMap<>();
        Map<String, Currency> currencies = new LinkedHashMap<>();
        Map<String, List<OrderLineView>> linesByOrder = new LinkedHashMap<>();

        while (resultSet.next()) {
            String rawOrderId = resultSet.getString("order_id");
            Currency currency = Currency.getInstance(resultSet.getString("currency"));

            orderIds.putIfAbsent(rawOrderId, OrderId.of(rawOrderId));
            statuses.putIfAbsent(rawOrderId, OrderStatus.valueOf(resultSet.getString("status")));
            currencies.putIfAbsent(rawOrderId, currency);
            linesByOrder.putIfAbsent(rawOrderId, new ArrayList<>());

            String rawProductId = resultSet.getString("product_id");
            if (rawProductId != null) {
                ProductId productId = new ProductId(rawProductId);
                int quantity = resultSet.getInt("quantity");
                Money unitPrice = new Money(new BigDecimal(resultSet.getString("unit_price")), currency);
                Money lineTotal = unitPrice.multiply(quantity);
                linesByOrder.get(rawOrderId).add(new OrderLineView(productId, quantity, unitPrice, lineTotal));
            }
        }

        List<OrderView> views = new ArrayList<>();
        for (String rawOrderId : orderIds.keySet()) {
            Currency currency = currencies.get(rawOrderId);
            List<OrderLineView> lines = linesByOrder.get(rawOrderId);
            Money total = lines.stream()
                    .map(OrderLineView::lineTotal)
                    .reduce(Money.zero(currency), Money::add);
            views.add(new OrderView(orderIds.get(rawOrderId), statuses.get(rawOrderId), lines, total));
        }
        return views;
    }
}