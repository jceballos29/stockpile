package dev.jceballos.stockpile.order.application.port;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

/**
 * Modelo de lectura de una línea de pedido: solo datos, para mostrar.
 * A diferencia de {@code OrderLine} (dominio), no tiene ningún método mas
 * allá de sus accesores generados -- el lado de lectura de CQRS no
 * necesita comportamiento, solo datos.
 *
 * @param productId  el producto de la línea
 * @param quantity   la cantidad
 * @param unitPrice  el precio unitario
 * @param lineTotal  el subtotal ya calculado (precio 'x' cantidad)
 */
public record OrderLineView(ProductId productId, int quantity, Money unitPrice, Money lineTotal) {
}