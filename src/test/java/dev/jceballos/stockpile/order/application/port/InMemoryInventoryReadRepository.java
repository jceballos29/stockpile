package dev.jceballos.stockpile.order.application.port;

import dev.jceballos.stockpile.shared.ProductId;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación en memoria de {@code InventoryReadRepository}, para
 * testear handlers de {@code order} sin depender de {@code inventory} ni
 * de SQLite.
 */
public class InMemoryInventoryReadRepository implements InventoryReadRepository {

    private final Map<ProductId, Integer> stock = new HashMap<>();

    @Override
    public int availableStockOf(ProductId productId) {
        Integer available = stock.get(productId);
        if (available == null) {
            throw new ProductNotFoundException(productId);
        }
        return available;
    }

    public void withStock(ProductId productId, int quantity) {
        stock.put(productId, quantity);
    }
}