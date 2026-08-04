package dev.jceballos.stockpile.order.application.query;

import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.OrderQuery;
import dev.jceballos.stockpile.order.application.port.OrderReadRepository;
import dev.jceballos.stockpile.order.application.port.OrderView;
import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.shared.PagedResult;

import java.util.Objects;

/**
 * Lado de lectura de CQRS para {@code order}: solo depende de
 * {@code OrderReadRepository}, sin ninguna referencia a
 * {@code OrderWriteRepository} ni a ningún puerto de {@code inventory} --
 * garantía en tiempo de compilación de que las consultas no pueden mutar
 * nada, en ninguno de los dos contextos.
 */
public class OrderQueryHandler {

    private final OrderReadRepository orderReadRepository;

    public OrderQueryHandler(OrderReadRepository orderReadRepository) {
        this.orderReadRepository = Objects.requireNonNull(orderReadRepository);
    }

    /**
     * @param orderId el pedido a buscar
     * @return el modelo de lectura del pedido
     * @throws OrderNotFoundException si no existe
     */
    public OrderView getById(OrderId orderId) {
        return orderReadRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * @param query los criterios de búsqueda
     * @return una página de resultados
     */
    public PagedResult<OrderView> list(OrderQuery query) {
        return orderReadRepository.findAll(query);
    }
}