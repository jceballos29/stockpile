package dev.jceballos.stockpile.shared.application.port;

/**
 * Fake de {@code UnitOfWork} para tests: ejecuta la acción directamente,
 * sin ninguna semántica transaccional real.
 * <p>
 * Es válido porque los repositorios en memoria (los demás Fakes del
 * proyecto) no tienen ningún modo de fallo parcial que una transacción
 * deba proteger -- a diferencia de la implementación SQLite (Fase 6),
 * que si necesita begin/commit/rollback de verdad. La prueba real de
 * atomicidad va a llegar recién con {@code SqliteUnitOfWork}, contra una
 * base real donde un fallo a mitad de camino es un escenario genuino.
 */
public class NoOpUnitOfWork implements UnitOfWork {

    @Override
    public void execute(Runnable action) {
        action.run();
    }
}