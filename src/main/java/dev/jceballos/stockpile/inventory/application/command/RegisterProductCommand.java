package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

/**
 * Comando: intención de registrar un producto nuevo en el catálogo.
 *
 * @param productId    la identidad del nuevo producto
 * @param name         el nombre
 * @param price        el precio unitario
 * @param initialStock el stock inicial
 */
public record RegisterProductCommand(ProductId productId, String name, Money price, int initialStock) {

    /**
     * @throws NullPointerException     si {@code productId}, {@code name} o {@code price} son nulos
     * @throws IllegalArgumentException si {@code name} esta vacio/en blanco, o si
     *                                  {@code initialStock} es negativo
     */
    public RegisterProductCommand {
        Objects.requireNonNull(productId, "El identificador del producto no puede ser nulo");
        Objects.requireNonNull(name, "El nombre del producto no puede ser nulo");
        Objects.requireNonNull(price, "El precio del producto no puede ser nulo");
        if (name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        if (initialStock < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }
    }
}