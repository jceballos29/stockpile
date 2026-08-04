package dev.jceballos.stockpile.infrastructure.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Crea las tablas del esquema si no existen ({@code orders},
 * {@code order_lines}, {@code products}). No inserta datos: el alta de
 * productos entra por {@code RegisterProductCommandHandler} (Fase 5), no
 * por SQL crudo que se saltearía sus invariantes.
 */
public class SchemaInitializer {

    /**
     * Crea el esquema completo si todavía no existe. Idempotente: se
     * puede llamar multiples veces sin error.
     *
     * @param connection la conexión sobre la que crear el esquema
     * @throws PersistenceException si alguna sentencia falla
     */
    public void initialize(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id TEXT PRIMARY KEY,
                        status TEXT NOT NULL,
                        currency TEXT NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS order_lines (
                        order_id TEXT NOT NULL,
                        product_id TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        unit_price TEXT NOT NULL,
                        PRIMARY KEY (order_id, product_id),
                        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        price TEXT NOT NULL,
                        stock INTEGER NOT NULL
                    )
                    """);
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo inicializar el esquema de la base de datos", e);
        }
    }
}