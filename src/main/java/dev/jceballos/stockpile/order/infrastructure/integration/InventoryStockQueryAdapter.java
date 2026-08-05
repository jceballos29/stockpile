package dev.jceballos.stockpile.order.infrastructure.integration;

import dev.jceballos.stockpile.inventory.application.port.ProductView;
import dev.jceballos.stockpile.inventory.application.query.ProductQueryHandler;
import dev.jceballos.stockpile.order.application.port.InventoryReadRepository;
import dev.jceballos.stockpile.order.application.port.ProductNotFoundException;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

/**
 * Anti-Corruption Layer: implementa {@code InventoryReadRepository} (el
 * puerto que {@code order} define para consultar stock) delegando en
 * {@code ProductQueryHandler}, el handler real de {@code inventory} --
 * mismo patron que {@code InventoryStockReservationAdapter}, ahora para
 * el lado de lectura. {@code order} nunca consulta la tabla
 * {@code products} directamente.
 */
public class InventoryStockQueryAdapter implements InventoryReadRepository {

    private final ProductQueryHandler productQueryHandler;

    public InventoryStockQueryAdapter(ProductQueryHandler productQueryHandler) {
        this.productQueryHandler = Objects.requireNonNull(productQueryHandler);
    }

    @Override
    public int availableStockOf(ProductId productId) {
        try {
            ProductView view = productQueryHandler.getById(productId);
            return view.stock();
        } catch (dev.jceballos.stockpile.inventory.domain.ProductNotFoundException e) {
            throw new ProductNotFoundException(productId);
        }
    }
}