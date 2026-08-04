package dev.jceballos.stockpile.infrastructure.persistence;

/**
 * Envuelve cualquier {@code SQLException} en una excepción unchecked, para
 * que ningún puerto de la capa application (ver order.application.port,
 * inventory.application.port) tenga que conocer JDBC ni declarar
 * {@code throws SQLException} en su firma.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}