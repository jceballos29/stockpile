package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

public record UpdateProductCommand(ProductId productId, String name, String description, Money price) {

    public UpdateProductCommand {
        Objects.requireNonNull(productId, "El identificador del producto no puede ser nulo");
        Objects.requireNonNull(name, "El nombre del producto no puede ser nulo");
        Objects.requireNonNull(description, "La descripcion no puede ser nula");
        Objects.requireNonNull(price, "El precio del producto no puede ser nulo");
        if (name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacio");
        }
    }
}