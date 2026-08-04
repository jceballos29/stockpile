package dev.jceballos.stockpile.order.application.port;

import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.shared.PagedResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion en memoria de {@code OrderReadRepository}, para testear
 * el handler de consultas sin SQLite.
 */
public class InMemoryOrderReadRepository implements OrderReadRepository {

    private final List<OrderView> views = new ArrayList<>();

    @Override
    public Optional<OrderView> findById(OrderId orderId) {
        return views.stream().filter(v -> v.orderId().equals(orderId)).findFirst();
    }

    @Override
    public PagedResult<OrderView> findAll(OrderQuery query) {
        List<OrderView> filtered = views.stream()
                .filter(v -> query.status().map(status -> status == v.status()).orElse(true))
                .toList();

        int fromIndex = Math.min(query.page() * query.pageSize(), filtered.size());
        int toIndex = Math.min(fromIndex + query.pageSize(), filtered.size());

        return new PagedResult<>(filtered.subList(fromIndex, toIndex), query.page(), query.pageSize(), filtered.size());
    }

    public void add(OrderView view) {
        views.add(view);
    }
}