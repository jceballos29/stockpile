package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.order.domain.OrderId;

import java.util.Objects;

/**
 * Comando: intención de cancelar un pedido abierto.
 *
 * @param orderId el pedido a cancelar
 */
public record CancelOrderCommand(OrderId orderId) {

    /**
     * @throws NullPointerException si {@code orderId} es nulo
     */
    public CancelOrderCommand {
        Objects.requireNonNull(orderId, "El identificador de la orden no puede ser nulo");
    }
}