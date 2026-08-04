package dev.jceballos.stockpile.order.application.port;

import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria de {@code OrderWriteRepository}, para testear
 * handlers de aplicación sin depender de SQLite.
 */
public class InMemoryOrderWriteRepository implements OrderWriteRepository {

    private final Map<OrderId, Order> storage = new HashMap<>();

    @Override
    public void save(Order order) {
        storage.put(order.orderId(), order);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return Optional.ofNullable(storage.get(orderId));
    }
}