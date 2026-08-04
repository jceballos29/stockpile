package dev.jceballos.stockpile.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Crea y configura una conexión JDBC a SQLite. Pensada para abrirse UNA
 * vez al arrancar la aplicación (Composition Root, Fase 7) y reutilizarse
 * durante toda la ejecución -- entre otras cosas, es lo que hace posible
 * que {@code UnitOfWork} coordine una transacción real entre {@code order}
 * e {@code inventory}.
 */
public class SqliteConnectionFactory {

    private final String jdbcUrl;

    public SqliteConnectionFactory(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Abre una conexión nueva, con las Foreign Keys activadas.
     *
     * @return una {@code Connection} lista para usar
     * @throws PersistenceException si no se pudo conectar
     */
    public Connection createConnection() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo conectar a la base de datos: " + jdbcUrl, e);
        }
    }
}