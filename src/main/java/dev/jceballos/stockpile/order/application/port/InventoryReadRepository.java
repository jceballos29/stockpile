package dev.jceballos.stockpile.order.application.port;


import dev.jceballos.stockpile.shared.ProductId;

/**
 * Puerto de solo lectura hacia el catálogo/inventario. Lo define y posee
 * {@code order} (el "cliente" en la relación Customer/Supplier con
 * {@code inventory}, ver brief.md sección 2) -- el dominio {@code Order}
 * nunca llama a este puerto directamente: lo consulta la capa de
 * aplicación (Fase 5), que le entrega el resultado a {@code Order} como
 * un simple {@code int} (ver Order.addLine, parámetro availableStock).
 */
public interface InventoryReadRepository {

    /**
     * Consulta el stock disponible de un producto en este momento.
     *
     * @param productId el producto a consultar
     * @return la cantidad disponible actual
     * @throws ProductNotFoundException si el producto no existe en el catálogo
     */
    int availableStockOf(ProductId productId);
}