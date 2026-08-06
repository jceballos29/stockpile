package dev.jceballos.stockpile.infrastructure.persistence;


import dev.jceballos.stockpile.shared.ProductId;
import dev.jceballos.stockpile.shared.ProductSalesView;
import dev.jceballos.stockpile.shared.application.port.SalesReportRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SqliteSalesReportRepository implements SalesReportRepository {

    private final Connection connection;

    public SqliteSalesReportRepository(Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public List<ProductSalesView> topSellingProducts(int limit) {
        String sql = """
                SELECT p.id, p.name, SUM(ol.quantity) AS total_quantity
                FROM order_lines ol
                JOIN products p ON p.id = ol.product_id
                GROUP BY p.id, p.name
                ORDER BY total_quantity DESC
                LIMIT ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProductSalesView> results = new ArrayList<>();
                while (resultSet.next()) {
                    ProductId productId = new ProductId(resultSet.getString("id"));
                    String name = resultSet.getString("name");
                    int quantitySold = resultSet.getInt("total_quantity");
                    results.add(new ProductSalesView(productId, name, quantitySold));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al calcular productos mas vendidos", e);
        }
    }
}