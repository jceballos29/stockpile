package dev.jceballos.stockpile.inventory.domain;

import dev.jceballos.stockpile.shared.ProductId;

/**
 * Se lanza cuando se busca un producto por su identidad y no existe en el
 * catálogo. Homónima de {@code order.application.port.ProductNotFoundException}
 * a propósito -- misma idea de negocio, tipos distintos, cada uno propio de
 * su contexto (ver brief.md, seccion 2).
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(ProductId productId) {
        super("No se encontró el producto: " + productId);
    }
}