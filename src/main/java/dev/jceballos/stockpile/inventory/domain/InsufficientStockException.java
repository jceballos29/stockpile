package dev.jceballos.stockpile.inventory.domain;

import dev.jceballos.stockpile.shared.ProductId;

/**
 * Se lanza cuando {@code Product.reserve()} rechaza una reserva porque la
 * cantidad solicitada supera el stock disponible.
 * <p>
 * Homónima de {@code order.domain.InsufficientStockException} a propósito:
 * ambas representan "no hay stock suficiente", pero son tipos distintos,
 * cada uno propio de su Bounded Context -- ver brief.md, sección 2, sobre
 * por qué los contextos no comparten vocabulario de dominio salvo lo que
 * está explícitamente en el Shared Kernel.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(ProductId productId, int requested, int available) {
        super("Stock insuficiente para el producto " + productId
                + ": solicitado=" + requested + ", disponible=" + available);
    }
}