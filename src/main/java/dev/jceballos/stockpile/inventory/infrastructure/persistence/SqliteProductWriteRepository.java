package dev.jceballos.stockpile.inventory.infrastructure.persistence;

import dev.jceballos.stockpile.infrastructure.persistence.PersistenceException;
import dev.jceballos.stockpile.inventory.application.port.ProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.shared.ProductId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador SQLite de {@code ProductWriteRepository}.
 * <p>
 * {@code save()} es un upsert directo del estado completo del agregado
 * (no un {@code UPDATE ... WHERE stock >= ?} condicional): en el modelo
 * de concurrencia de Stockpile -- una sola {@code Connection}, un solo
 * hilo, comandos ejecutados de a uno -- la atomicidad real de la reserva
 * ya la garantiza {@code Product.reserve()} en memoria. Un {@code UPDATE}
 * condicional a nivel SQL solo aportaría algo en un escenario con
 * multiples conexiones concurrentes (ej. un servidor web), que no es
 * el caso aca.
 */
public class SqliteProductWriteRepository implements ProductWriteRepository {

    private final Connection connection;

    public SqliteProductWriteRepository(Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public void save(Product product) {
        try {
            if (!update(product)) {
                insert(product);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al guardar el producto " + product.productId(), e);
        }
    }

    @Override
    public void deleteById(ProductId productId) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productId.value());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Error al eliminar el producto " + productId, e);
        }
    }

    private boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, currency = ?, stock = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindProductData(statement, product, 1);
            statement.setString(6, product.productId().value());
            return statement.executeUpdate() > 0;
        }
    }

    private void insert(Product product) throws SQLException {
        String sql = "INSERT INTO products (id, name, description, price, currency, stock) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.productId().value());
            bindProductData(statement, product, 2);
            statement.executeUpdate();
        }
    }

    private void bindProductData(PreparedStatement statement, Product product, int offset) throws SQLException {
        statement.setString(offset, product.name());
        statement.setString(offset + 1, product.description());
        statement.setString(offset + 2, product.price().amount().toPlainString());
        statement.setString(offset + 3, product.price().currency().getCurrencyCode());
        statement.setInt(offset + 4, product.stock());
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        String sql = "SELECT name, description, price, currency, stock FROM products WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productId.value());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return Optional.empty();
                var data = ProductRowMapper.extractCommonData(resultSet);

                return Optional.of(Product.reconstitute(
                        productId,
                        data.name(),
                        data.description(),
                        data.price(),
                        data.stock()
                ));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar el producto " + productId, e);
        }
    }
}