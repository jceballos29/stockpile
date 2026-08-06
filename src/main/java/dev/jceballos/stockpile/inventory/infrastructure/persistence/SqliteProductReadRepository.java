package dev.jceballos.stockpile.inventory.infrastructure.persistence;

import dev.jceballos.stockpile.infrastructure.persistence.PersistenceException;
import dev.jceballos.stockpile.inventory.application.port.ProductQuery;
import dev.jceballos.stockpile.inventory.application.port.ProductReadRepository;
import dev.jceballos.stockpile.inventory.application.port.ProductView;
import dev.jceballos.stockpile.shared.PagedResult;
import dev.jceballos.stockpile.shared.ProductId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador SQLite de {@code ProductReadRepository}. {@code findAll}
 * ejecuta dos consultas: una para la página pedida ({@code LIMIT}/
 * {@code OFFSET}), otra ({@code COUNT}) para {@code totalElements} -- a
 * diferencia de un Stream en memoria, SQL no te da "el tamaño que habría
 * sin el límite" gratis junto con los resultados.
 */
public class SqliteProductReadRepository implements ProductReadRepository {

    private final Connection connection;

    public SqliteProductReadRepository(Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public Optional<ProductView> findById(ProductId productId) {
        String sql = "SELECT id, name, description, price, currency, stock FROM products WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productId.value());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(toView(resultSet));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar el producto " + productId, e);
        }
    }

    @Override
    public PagedResult<ProductView> findAll(ProductQuery query) {
        String nameFilter = "%" + query.nameContains().orElse("") + "%";

        try {
            long totalElements = countMatching(nameFilter);
            List<ProductView> items = findPage(nameFilter, query.page(), query.pageSize());
            return new PagedResult<>(items, query.page(), query.pageSize(), totalElements);
        } catch (SQLException e) {
            throw new PersistenceException("Error al listar productos", e);
        }
    }

    private long countMatching(String nameFilter) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE name LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nameFilter);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private List<ProductView> findPage(String nameFilter, int page, int pageSize) throws SQLException {
        String sql = "SELECT id, name, description, price, currency, stock FROM products WHERE name LIKE ? "
                + "ORDER BY id LIMIT ? OFFSET ?";
        List<ProductView> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nameFilter);
            statement.setInt(2, pageSize);
            statement.setInt(3, page * pageSize);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(toView(resultSet));
                }
            }
        }
        return items;
    }

    private ProductView toView(ResultSet resultSet) throws SQLException {
        ProductId productId = new ProductId(resultSet.getString("id"));
        var data = ProductRowMapper.extractCommonData(resultSet);

        return new ProductView(
                productId,
                data.name(),
                data.description(),
                data.price(),
                data.stock()
        );
    }
}
