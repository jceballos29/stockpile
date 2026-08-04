package dev.jceballos.stockpile.infrastructure.persistence;

import dev.jceballos.stockpile.shared.application.port.UnitOfWork;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Implementación real de {@code UnitOfWork}: coordina una transacción SQL
 * genuina sobre la {@code Connection} compartida. Es lo que hace posible
 * que {@code AddProductToOrderCommandHandler}/{@code CancelOrderCommandHandler}
 * garanticen consistencia fuerte entre {@code order} e {@code inventory}
 * -- ambos contextos escriben, en la misma operación, sobre la misma base.
 */
public class SqliteUnitOfWork implements UnitOfWork {

    private final Connection connection;

    public SqliteUnitOfWork(Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public void execute(Runnable action) {
        try {
            connection.setAutoCommit(false);
            action.run();
            connection.commit();
        } catch (RuntimeException e) {
            rollbackQuietly();
            throw e;
        } catch (SQLException e) {
            rollbackQuietly();
            throw new PersistenceException("Error al coordinar la transacción", e);
        } finally {
            restoreAutoCommit();
        }
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Mismo criterio que en los repositorios: si el rollback mismo
            // falla, no hay mejor recuperación posible desde aca.
        }
    }

    private void restoreAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}