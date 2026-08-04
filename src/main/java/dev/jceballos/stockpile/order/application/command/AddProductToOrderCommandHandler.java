package dev.jceballos.stockpile.order.application.command;
import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.InventoryReadRepository;
import dev.jceballos.stockpile.order.application.port.OrderWriteRepository;
import dev.jceballos.stockpile.order.application.port.StockReservationPort;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.shared.application.port.UnitOfWork;

import java.util.Objects;

public class AddProductToOrderCommandHandler {

    private final OrderWriteRepository orderWriteRepository;
    private final InventoryReadRepository inventoryReadRepository;
    private final StockReservationPort stockReservationPort;
    private final UnitOfWork unitOfWork;

    public AddProductToOrderCommandHandler(OrderWriteRepository orderWriteRepository,
                                           InventoryReadRepository inventoryReadRepository,
                                           StockReservationPort stockReservationPort,
                                           UnitOfWork unitOfWork) {
        this.orderWriteRepository = Objects.requireNonNull(orderWriteRepository);
        this.inventoryReadRepository = Objects.requireNonNull(inventoryReadRepository);
        this.stockReservationPort = Objects.requireNonNull(stockReservationPort);
        this.unitOfWork = Objects.requireNonNull(unitOfWork);
    }

    /**
     * Ejecuta el comando, coordinado como una unidad atómica por
     * {@code unitOfWork}.
     *
     * @param command los datos de la línea a agregar
     * @throws OrderNotFoundException si el pedido no existe
     */
    public void handle(AddProductToOrderCommand command) {
        unitOfWork.execute(() -> {
            Order order = orderWriteRepository.findById(command.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

            int availableStock = inventoryReadRepository.availableStockOf(command.productId());

            order.addLine(command.productId(), command.quantity(), command.unitPrice(), availableStock);

            stockReservationPort.reserve(command.productId(), command.quantity());

            orderWriteRepository.save(order);
        });
    }
}