package dev.jceballos.stockpile.inventory.infrastructure.persistence;

import dev.jceballos.stockpile.infrastructure.persistence.PersistenceException;
import dev.jceballos.stockpile.inventory.application.port.ProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
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

    private boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, price = ?, currency = ?, stock = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.name());
            statement.setString(2, product.price().amount().toPlainString());
            statement.setString(3, product.price().currency().getCurrencyCode());
            statement.setInt(4, product.stock());
            statement.setString(5, product.productId().value());
            return statement.executeUpdate() > 0;
        }
    }

    private void insert(Product product) throws SQLException {
        String sql = "INSERT INTO products (id, name, price, currency, stock) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.productId().value());
            statement.setString(2, product.name());
            statement.setString(3, product.price().amount().toPlainString());
            statement.setString(4, product.price().currency().getCurrencyCode());
            statement.setInt(5, product.stock());
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        String sql = "SELECT name, price, currency, stock FROM products WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productId.value());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                String name = resultSet.getString("name");
                Currency currency = Currency.getInstance(resultSet.getString("currency"));
                Money price = new Money(new BigDecimal(resultSet.getString("price")), currency);
                int stock = resultSet.getInt("stock");
                return Optional.of(Product.reconstitute(productId, name, price, stock));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar el producto " + productId, e);
        }
    }
}