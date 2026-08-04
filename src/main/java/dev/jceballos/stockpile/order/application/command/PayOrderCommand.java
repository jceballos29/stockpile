package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.order.domain.OrderId;

import java.util.Objects;

/**
 * Comando: intención de pagar un pedido abierto.
 *
 * @param orderId el pedido a pagar
 */
public record PayOrderCommand(OrderId orderId) {

    /**
     * @throws NullPointerException si {@code orderId} es nulo
     */
    public PayOrderCommand {
        Objects.requireNonNull(orderId, "El identificador de la orden no puede ser nulo");
    }
}