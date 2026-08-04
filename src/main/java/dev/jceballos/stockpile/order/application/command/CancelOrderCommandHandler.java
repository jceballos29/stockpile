package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.OrderWriteRepository;
import dev.jceballos.stockpile.order.application.port.StockReservationPort;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderLine;

import java.util.Objects;

/**
 * Orquesta la cancelación de un pedido: valida la transición de estado
 * (delegada al dominio, ver {@code Order.cancel()}), libera el stock
 * reservado por cada línea, y persiste el pedido cancelado.
 */
public class CancelOrderCommandHandler {

    private final OrderWriteRepository orderWriteRepository;
    private final StockReservationPort stockReservationPort;

    public CancelOrderCommandHandler(OrderWriteRepository orderWriteRepository,
                                     StockReservationPort stockReservationPort) {
        this.orderWriteRepository = Objects.requireNonNull(orderWriteRepository);
        this.stockReservationPort = Objects.requireNonNull(stockReservationPort);
    }

    /**
     * Ejecuta el comando.
     *
     * @param command el pedido a cancelar
     * @throws OrderNotFoundException si el pedido no existe
     * @throws dev.jceballos.stockpile.order.domain.InvalidOrderStateException
     *         si el pedido no está OPEN
     */
    public void handle(CancelOrderCommand command) {
        Order order = orderWriteRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        order.cancel();

        for (OrderLine line : order.lines()) {
            stockReservationPort.release(line.productId(), line.quantity());
        }

        orderWriteRepository.save(order);
    }
}