package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.OrderWriteRepository;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.shared.application.port.UnitOfWork;

import java.util.Objects;

/**
 * Orquesta el pago de un pedido: carga, delega la validación de estado y
 * de líneas al dominio ({@code Order.pay()}), y persiste.
 */
public class PayOrderCommandHandler {

    private final OrderWriteRepository orderWriteRepository;
    private final UnitOfWork unitOfWork;

    public PayOrderCommandHandler(OrderWriteRepository orderWriteRepository, UnitOfWork unitOfWork) {
        this.orderWriteRepository = Objects.requireNonNull(orderWriteRepository);
        this.unitOfWork = Objects.requireNonNull(unitOfWork);
    }

    /**
     * Ejecuta el comando.
     *
     * @param command el pedido a pagar
     * @throws OrderNotFoundException si el pedido no existe
     * @throws dev.jceballos.stockpile.order.domain.InvalidOrderStateException
     *         si el pedido no esta OPEN, o si no tiene lineas
     */
    public void handle(PayOrderCommand command) {
        unitOfWork.execute(() -> {
            Order order = orderWriteRepository.findById(command.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

            order.pay();

            orderWriteRepository.save(order);
        });
    }
}