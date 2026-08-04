package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.InventoryReadRepository;
import dev.jceballos.stockpile.order.application.port.OrderWriteRepository;
import dev.jceballos.stockpile.order.application.port.StockReservationPort;
import dev.jceballos.stockpile.order.domain.Order;

import java.util.Objects;

/**
 * Orquesta el caso de uso completo "agregar producto al pedido": carga el
 * pedido, valida en memoria contra el stock consultado (defensa en
 * profundidad de {@code Order.addLine}), reserva el stock real de forma
 * atómica, y persiste el pedido actualizado.
 */
public class AddProductToOrderCommandHandler {

    private final OrderWriteRepository orderWriteRepository;
    private final InventoryReadRepository inventoryReadRepository;
    private final StockReservationPort stockReservationPort;

    public AddProductToOrderCommandHandler(OrderWriteRepository orderWriteRepository,
                                           InventoryReadRepository inventoryReadRepository,
                                           StockReservationPort stockReservationPort) {
        this.orderWriteRepository = Objects.requireNonNull(orderWriteRepository);
        this.inventoryReadRepository = Objects.requireNonNull(inventoryReadRepository);
        this.stockReservationPort = Objects.requireNonNull(stockReservationPort);
    }

    /**
     * Ejecuta el comando.
     *
     * @param command los datos de la línea a agregar
     * @throws OrderNotFoundException si el pedido no existe
     */
    public void handle(AddProductToOrderCommand command) {
        Order order = orderWriteRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        int availableStock = inventoryReadRepository.availableStockOf(command.productId());

        order.addLine(command.productId(), command.quantity(), command.unitPrice(), availableStock);

        stockReservationPort.reserve(command.productId(), command.quantity());

        orderWriteRepository.save(order);
    }
}