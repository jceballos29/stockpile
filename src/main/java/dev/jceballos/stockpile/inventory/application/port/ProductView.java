package dev.jceballos.stockpile.inventory.application.port;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

/**
 * Modelo de lectura de un producto: solo datos, para mostrar. Mismo
 * criterio que {@code order.application.port.OrderView}: sin
 * comportamiento, el agregado {@code Product} nunca se expone directo
 * al lado de lectura de CQRS.
 *
 * @param productId el identificador del producto
 * @param name      el nombre
 * @param price     el precio unitario
 * @param stock     el stock actual disponible
 */
public record ProductView(ProductId productId, String name, String description, Money price, int stock) {
}