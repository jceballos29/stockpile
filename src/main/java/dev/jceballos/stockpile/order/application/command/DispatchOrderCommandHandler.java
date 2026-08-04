package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.OrderWriteRepository;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.shared.application.port.UnitOfWork;

import java.util.Objects;

/**
 * Orquesta el despacho de un pedido: carga, delega la validación de
 * estado al dominio ({@code Order.dispatch()}), y persiste.
 */
public class DispatchOrderCommandHandler {

    private final OrderWriteRepository orderWriteRepository;
    private final UnitOfWork unitOfWork;

    public DispatchOrderCommandHandler(OrderWriteRepository orderWriteRepository, UnitOfWork unitOfWork) {
        this.orderWriteRepository = Objects.requireNonNull(orderWriteRepository);
        this.unitOfWork = Objects.requireNonNull(unitOfWork);
    }

    /**
     * Ejecuta el comando.
     *
     * @param command el pedido a despachar
     * @throws OrderNotFoundException si el pedido no existe
     * @throws dev.jceballos.stockpile.order.domain.InvalidOrderStateException
     *         si el pedido no está PAID
     */
    public void handle(DispatchOrderCommand command) {
        unitOfWork.execute(() -> {
            Order order = orderWriteRepository.findById(command.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

            order.dispatch();

            orderWriteRepository.save(order);
        });
    }
}