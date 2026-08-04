package dev.jceballos.stockpile.infrastructure.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SchemaInitializerTest {

    private Connection connection;

    @BeforeEach
    void setUp() {
        connection = new SqliteConnectionFactory("jdbc:sqlite::memory:").createConnection();
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void shouldCreateOrdersTable() {
        new SchemaInitializer().initialize(connection);

        assertThat(tableExists("orders")).isTrue();
    }

    @Test
    void shouldCreateOrderLinesTable() {
        new SchemaInitializer().initialize(connection);

        assertThat(tableExists("order_lines")).isTrue();
    }

    @Test
    void shouldCreateProductsTable() {
        new SchemaInitializer().initialize(connection);

        assertThat(tableExists("products")).isTrue();
    }

    @Test
    void shouldBeIdempotentWhenInitializedTwice() {
        new SchemaInitializer().initialize(connection);

        assertThatCode(() -> new SchemaInitializer().initialize(connection))
                .doesNotThrowAnyException();
    }

    private boolean tableExists(String tableName) {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}