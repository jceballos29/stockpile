package dev.jceballos.stockpile.order.domain;

import dev.jceballos.stockpile.shared.ProductId;

/**
 * Se lanza cuando {@code Order.addLine()} rechaza agregar una línea porque
 * la cantidad total solicitada para un producto excede el stock disponible
 * que se le informó.
 * <p>
 * Es unchecked (extiende {@code RuntimeException}) a propósito: representa
 * una regla de negocio violada, no un error que quien llama a
 * {@code addLine()} pueda resolver ahi mismo -- se propaga hasta un punto
 * centralizado que decida como mostrarla.
 */
public class InsufficientStockException extends RuntimeException {

    /**
     * @param productId el producto para el que no alcanza el stock
     * @param requested la cantidad total solicitada (acumulada)
     * @param available el stock disponible informado
     */
    public InsufficientStockException(ProductId productId, int requested, int available) {
        super("Stock insuficiente para el producto " + productId
                + ": solicitado=" + requested + ", disponible=" + available);
    }
}
