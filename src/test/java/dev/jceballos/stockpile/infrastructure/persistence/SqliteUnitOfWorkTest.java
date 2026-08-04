package dev.jceballos.stockpile.infrastructure.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqliteUnitOfWorkTest {

    private Connection connection;
    private SqliteUnitOfWork unitOfWork;

    @BeforeEach
    void setUp() throws SQLException {
        connection = new SqliteConnectionFactory("jdbc:sqlite::memory:").createConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE counter (id INTEGER PRIMARY KEY, value INTEGER NOT NULL)");
            statement.execute("INSERT INTO counter (id, value) VALUES (1, 0)");
        }
        unitOfWork = new SqliteUnitOfWork(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void shouldCommitAllChangesWhenActionSucceeds() {
        unitOfWork.execute(() -> {
            incrementCounter();
            incrementCounter();
        });

        assertThat(readCounter()).isEqualTo(2);
    }

    @Test
    void shouldRollbackAllChangesWhenActionThrows() {
        assertThatThrownBy(() -> unitOfWork.execute(() -> {
            incrementCounter();
            throw new IllegalStateException("fallo simulado a mitad de camino");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(readCounter()).isEqualTo(0);
    }

    @Test
    void shouldRestoreAutoCommitAfterSuccess() throws SQLException {
        unitOfWork.execute(this::incrementCounter);

        assertThat(connection.getAutoCommit()).isTrue();
    }

    @Test
    void shouldRestoreAutoCommitAfterFailure() {
        assertThatThrownBy(() -> unitOfWork.execute(() -> {
            throw new IllegalStateException("fallo");
        }));

        assertThatCode(() -> assertThat(connection.getAutoCommit()).isTrue())
                .doesNotThrowAnyException();
    }

    private void incrementCounter() {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE counter SET value = value + 1 WHERE id = 1")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int readCounter() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT value FROM counter WHERE id = 1")) {
            resultSet.next();
            return resultSet.getInt("value");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}