package dev.jceballos.stockpile.order.application.port;

import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.shared.PagedResult;

import java.util.Optional;

/**
 * Puerto de lectura para el agregado {@code Order}. Devuelve modelos de
 * lectura ({@code OrderView}), nunca el agregado {@code Order} en sí --
 * ver la explicación de OrderView/OrderLineView sobre por qué CQRS
 * separa estos dos modelos.
 */
public interface OrderReadRepository {

    /**
     * Busca un pedido por su identidad, para mostrarlo.
     *
     * @param orderId el identificador del pedido
     * @return el modelo de lectura del pedido, si existe
     */
    Optional<OrderView> findById(OrderId orderId);

    /**
     * Lista pedidos según los criterios de {@code query} (filtro por
     * estado, paginación).
     *
     * @param query los criterios de búsqueda
     * @return una página de resultados
     */
    PagedResult<OrderView> findAll(OrderQuery query);
}