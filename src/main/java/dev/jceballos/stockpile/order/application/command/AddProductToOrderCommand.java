package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

/**
 * Comando: intención de agregar un producto a un pedido.
 * <p>
 * Recibe {@code unitPrice} directamente, como si ya viniera resuelto desde
 * afuera (por ejemplo, del catálogo consultado en la UI antes de armar
 * este comando) -- Stockpile no modela un servicio de precios separado
 * del stock; es una simplificación consciente de alcance.
 *
 * @param orderId   el pedido al que se agrega la línea
 * @param productId el producto a agregar
 * @param quantity  la cantidad a agregar
 * @param unitPrice el precio unitario acordado
 */
public record AddProductToOrderCommand(OrderId orderId, ProductId productId, int quantity, Money unitPrice) {

    /**
     * @throws NullPointerException     si algín parámetro de referencia es nulo
     * @throws IllegalArgumentException si {@code quantity} no es mayor a cero
     */
    public AddProductToOrderCommand {
        Objects.requireNonNull(orderId, "El identificador de la orden no puede ser nulo");
        Objects.requireNonNull(productId, "El producto no puede ser nulo");
        Objects.requireNonNull(unitPrice, "El precio unitario no puede ser nulo");
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }
}