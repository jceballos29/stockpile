package dev.jceballos.stockpile.shared.application.port;

/**
 * Puerto que coordina una transacción abarcando multiples operaciones de
 * escritura -- típicamente, entre agregados de distintos Bounded Context
 * que necesitan consistencia fuerte (ver brief.md, sección 5: Order e
 * Inventory, en la misma base de datos, coordinados via una transacción
 * SQL real en vez de eventual consistency).
 * <p>
 * Vive en {@code shared}: no es específico de ningún contexto, sino de
 * cualquier handler que necesite orquestar más de un agregado como una
 * sola unidad atómica.
 * <p>
 * La implementación real (Fase 6, {@code SqliteUnitOfWork}) recién es
 * posible una vez que existe una {@code Connection} compartida para hacer
 * begin/commit/rollback de verdad.
 */
public interface UnitOfWork {

    /**
     * Ejecuta {@code action} de forma atómica: si se completa sin lanzar
     * ninguna excepción, confirma todos los cambios; si lanza cualquier
     * excepción, deshace todo lo que se alcanzo a hacer y propaga la
     * excepción original sin modificarla.
     *
     * @param action el código a ejecutar
     */
    void execute(Runnable action);
}