package dev.jceballos.stockpile.order.application.port;

import dev.jceballos.stockpile.shared.ProductId;

/**
 * Puerto de escritura hacia el inventario: reservar y devolver stock de
 * forma real y atómica (no una simple validación de lectura).
 * <p>
 * Lo define {@code order} por el mismo motivo que {@code
 * InventoryReadRepository} -- es lo que el caso de uso "agregar producto
 * al pedido" (Fase 5) necesita de {@code inventory}. La implementación
 * real (Fase 5/6) va a delegar en los command handlers propios del
 * contexto {@code inventory}, actuando como una Anti-Corruption Layer:
 * traduce entre el vocabulario de {@code order} y el de {@code inventory}
 * (incluidas las excepciones -- ver ProductNotFoundException e
 * InsufficientStockException, ambas con homónimas en inventory.domain).
 */
public interface StockReservationPort {

    /**
     * Reserva (descuenta) stock real de un producto.
     *
     * @param productId el producto
     * @param quantity  la cantidad a reservar
     * @throws ProductNotFoundException                              si el producto no existe
     * @throws dev.jceballos.stockpile.order.domain.InsufficientStockException
     *         si no hay stock suficiente
     */
    void reserve(ProductId productId, int quantity);

    /**
     * Devuelve stock previamente reservado -- usado al cancelar un pedido.
     *
     * @param productId el producto
     * @param quantity  la cantidad a devolver
     * @throws ProductNotFoundException si el producto no existe
     */
    void release(ProductId productId, int quantity);
}