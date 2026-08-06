package dev.jceballos.stockpile.order.application.port;

import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.order.domain.OrderStatus;
import dev.jceballos.stockpile.shared.Money;

import java.time.Instant;
import java.util.List;

/**
 * Modelo de lectura de un pedido completo: solo datos, para mostrar.
 * A propósito NO tiene métodos como {@code pay()} o {@code addLine()} --
 * ese comportamiento pertenece únicamente al agregado {@code Order}.
 *
 * @param orderId el identificador del pedido
 * @param status  el estado actual
 * @param createdAt la fecha de creación
 * @param lines   las líneas del pedido, como modelos de lectura
 * @param total   el total ya calculado
 */
public record OrderView(OrderId orderId, OrderStatus status, Instant createdAt, List<OrderLineView> lines, Money total) {
}