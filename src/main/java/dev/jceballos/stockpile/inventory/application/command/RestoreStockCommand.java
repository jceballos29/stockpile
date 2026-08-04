package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

/**
 * Comando: intención de restituir (devolver) una cantidad de stock
 * previamente reservada -- típicamente al cancelar un pedido.
 *
 * @param productId el producto
 * @param quantity  la cantidad a restituir
 */
public record RestoreStockCommand(ProductId productId, int quantity) {

    /**
     * @throws NullPointerException     si {@code productId} es nulo
     * @throws IllegalArgumentException si {@code quantity} no es mayor a cero
     */
    public RestoreStockCommand {
        Objects.requireNonNull(productId, "El producto no puede ser nulo");
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad a restituir debe ser mayor a cero");
        }
    }
}