package dev.jceballos.stockpile.inventory.domain;

import dev.jceballos.stockpile.shared.ProductId;

/**
 * Se lanza cuando {@code Product.reserve()} rechaza una reserva porque la
 * cantidad solicitada supera el stock disponible.
 * <p>
 * Expone {@code requested()}/{@code available()} (no solo el mensaje) para
 * que quien la atrape -- tipicamente {@code InventoryStockReservationAdapter},
 * la Anti-Corruption Layer hacía {@code order} -- pueda reconstruir la
 * excepción equivalente de {@code order.domain} con los mismos datos.
 * <p>
 * Homóima de {@code order.domain.InsufficientStockException} a propósito:
 * ambas representan "no hay stock suficiente", pero son tipos distintos,
 * cada uno propio de su Bounded Context.
 */
public class InsufficientStockException extends RuntimeException {

    private final ProductId productId;
    private final int requested;
    private final int available;

    public InsufficientStockException(ProductId productId, int requested, int available) {
        super("Stock insuficiente para el producto " + productId
                + ": solicitado=" + requested + ", disponible=" + available);
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public ProductId productId() {
        return productId;
    }

    public int requested() {
        return requested;
    }

    public int available() {
        return available;
    }
}