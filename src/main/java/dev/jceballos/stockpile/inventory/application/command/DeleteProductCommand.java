package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

public record DeleteProductCommand(ProductId productId) {

    public DeleteProductCommand {
        Objects.requireNonNull(productId, "El identificador del producto no puede ser nulo");
    }
}