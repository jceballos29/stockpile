package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.order.domain.OrderId;

import java.util.Objects;

/**
 * Comando: intención de despachar un pedido ya pagado.
 *
 * @param orderId el pedido a despachar
 */
public record DispatchOrderCommand(OrderId orderId) {

    /**
     * @throws NullPointerException si {@code orderId} es nulo
     */
    public DispatchOrderCommand {
        Objects.requireNonNull(orderId, "El identificador de la orden no puede ser nulo");
    }
}