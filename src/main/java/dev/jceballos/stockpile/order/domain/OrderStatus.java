package dev.jceballos.stockpile.order.domain;

/**
 * Estados del ciclo de vida de un {@code Order}.
 * <p>
 * Las transiciones válidas ({@code OPEN -> PAID -> DISPATCHED},
 * {@code OPEN -> CANCELLED}) y sus guardas se implementan como
 * comportamiento del agregado {@code Order}, no aquí: un enum no
 * debería contener lógica de negocio. El orden en que se declaran
 * estas constantes no impone ninguna regla de transición por sí solo.
 */
public enum OrderStatus {
    OPEN,
    PAID,
    DISPATCHED,
    CANCELLED
}
