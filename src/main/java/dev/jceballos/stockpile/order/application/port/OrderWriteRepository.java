package dev.jceballos.stockpile.order.application.port;

import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderId;

import java.util.Optional;

/**
 * Puerto de escritura para el agregado {@code Order}. La implementación
 * real (Fase 6, SQLite) vive en {@code order.infrastructure.persistence};
 * este contrato no sabe -ni le importa- como se persiste.
 */
public interface OrderWriteRepository {

    /**
     * Guarda el estado actual del pedido -- inserta si es nuevo, actualiza
     * si ya existía.
     *
     * @param order el pedido a guardar
     */
    void save(Order order);

    /**
     * Busca un pedido por su identidad, para cargarlo, mutarlo (via sus
     * propios métodos de dominio) y volver a guardarlo.
     *
     * @param orderId el identificador del pedido
     * @return el pedido, si existe
     */
    Optional<Order> findById(OrderId orderId);
}