package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.OrderWriteRepository;
import dev.jceballos.stockpile.order.application.port.StockReservationPort;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderLine;
import dev.jceballos.stockpile.shared.application.port.UnitOfWork;

import java.util.Objects;

public class CancelOrderCommandHandler {

    private final OrderWriteRepository orderWriteRepository;
    private final StockReservationPort stockReservationPort;
    private final UnitOfWork unitOfWork;

    public CancelOrderCommandHandler(OrderWriteRepository orderWriteRepository,
                                     StockReservationPort stockReservationPort,
                                     UnitOfWork unitOfWork) {
        this.orderWriteRepository = Objects.requireNonNull(orderWriteRepository);
        this.stockReservationPort = Objects.requireNonNull(stockReservationPort);
        this.unitOfWork = Objects.requireNonNull(unitOfWork);
    }

    /**
     * Ejecuta el comando, coordinado como una unidad atómica por
     * {@code unitOfWork}.
     *
     * @param command el pedido a cancelar
     * @throws OrderNotFoundException si el pedido no existe
     */
    public void handle(CancelOrderCommand command) {
        unitOfWork.execute(() -> {
            Order order = orderWriteRepository.findById(command.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

            order.cancel();

            for (OrderLine line : order.lines()) {
                stockReservationPort.release(line.productId(), line.quantity());
            }

            orderWriteRepository.save(order);
        });
    }
}