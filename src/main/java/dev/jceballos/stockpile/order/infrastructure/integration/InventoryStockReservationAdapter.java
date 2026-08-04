package dev.jceballos.stockpile.order.infrastructure.integration;

import dev.jceballos.stockpile.inventory.application.command.ReserveStockCommand;
import dev.jceballos.stockpile.inventory.application.command.ReserveStockCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.RestoreStockCommand;
import dev.jceballos.stockpile.inventory.application.command.RestoreStockCommandHandler;
import dev.jceballos.stockpile.order.application.port.ProductNotFoundException;
import dev.jceballos.stockpile.order.application.port.StockReservationPort;
import dev.jceballos.stockpile.order.domain.InsufficientStockException;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

/**
 * Anti-Corruption Layer: implementa {@code StockReservationPort} (el
 * contrato que {@code order} define para lo que necesita de
 * {@code inventory}) delegando en los command handlers reales de
 * {@code inventory}, y traduciendo sus excepciones al vocabulario de
 * {@code order}.
 * <p>
 * No conoce SQLite ni ningun detalle de persistencia -- por eso vive en
 * {@code infrastructure.integration}, no en {@code infrastructure.persistence}:
 * es infraestructura en el sentido de "como alcanzo algo fuera de mis
 * propios limites", no en el sentido de "hablo con una base de datos".
 */
public class InventoryStockReservationAdapter implements StockReservationPort {

    private final ReserveStockCommandHandler reserveStockCommandHandler;
    private final RestoreStockCommandHandler restoreStockCommandHandler;

    public InventoryStockReservationAdapter(ReserveStockCommandHandler reserveStockCommandHandler,
                                            RestoreStockCommandHandler restoreStockCommandHandler) {
        this.reserveStockCommandHandler = Objects.requireNonNull(reserveStockCommandHandler);
        this.restoreStockCommandHandler = Objects.requireNonNull(restoreStockCommandHandler);
    }

    @Override
    public void reserve(ProductId productId, int quantity) {
        try {
            reserveStockCommandHandler.handle(new ReserveStockCommand(productId, quantity));
        } catch (dev.jceballos.stockpile.inventory.domain.ProductNotFoundException e) {
            throw new ProductNotFoundException(productId);
        } catch (dev.jceballos.stockpile.inventory.domain.InsufficientStockException e) {
            throw new InsufficientStockException(productId, e.requested(), e.available());
        }
    }

    @Override
    public void release(ProductId productId, int quantity) {
        try {
            restoreStockCommandHandler.handle(new RestoreStockCommand(productId, quantity));
        } catch (dev.jceballos.stockpile.inventory.domain.ProductNotFoundException e) {
            throw new ProductNotFoundException(productId);
        }
    }
}