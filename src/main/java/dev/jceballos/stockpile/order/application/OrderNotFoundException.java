package dev.jceballos.stockpile.order.application;

import dev.jceballos.stockpile.order.domain.OrderId;

/**
 * Se lanza cuando un handler busca un pedido por su identidad y no existe.
 * Vive en la raiz de {@code application} (no en {@code .port} ni en
 * {@code domain}): es una falla de orquestacion del caso de uso, decidida
 * por el handler que llama a {@code findById}, no un contrato declarado
 * por el puerto en sí.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId orderId) {
        super("No se encontró el pedido: " + orderId);
    }
}