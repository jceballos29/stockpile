package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

/**
 * Comando: intención de reservar (descontar) una cantidad de stock de un
 * producto.
 *
 * @param productId el producto
 * @param quantity  la cantidad a reservar
 */
public record ReserveStockCommand(ProductId productId, int quantity) {

    /**
     * @throws NullPointerException     si {@code productId} es nulo
     * @throws IllegalArgumentException si {@code quantity} no es mayor a cero
     */
    public ReserveStockCommand {
        Objects.requireNonNull(productId, "El producto no puede ser nulo");
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad a reservar debe ser mayor a cero");
        }
    }
}