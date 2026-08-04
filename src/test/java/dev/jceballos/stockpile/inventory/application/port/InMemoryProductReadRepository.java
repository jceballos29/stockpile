package dev.jceballos.stockpile.inventory.application.port;

import dev.jceballos.stockpile.shared.PagedResult;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación en memoria de {@code ProductReadRepository}, para testear
 * el handler de consultas sin SQLite.
 */
public class InMemoryProductReadRepository implements ProductReadRepository {

    private final List<ProductView> views = new ArrayList<>();

    @Override
    public Optional<ProductView> findById(ProductId productId) {
        return views.stream().filter(v -> v.productId().equals(productId)).findFirst();
    }

    @Override
    public PagedResult<ProductView> findAll(ProductQuery query) {
        List<ProductView> filtered = views.stream()
                .filter(v -> query.nameContains().map(fragment -> v.name().contains(fragment)).orElse(true))
                .toList();

        int fromIndex = Math.min(query.page() * query.pageSize(), filtered.size());
        int toIndex = Math.min(fromIndex + query.pageSize(), filtered.size());

        return new PagedResult<>(filtered.subList(fromIndex, toIndex), query.page(), query.pageSize(), filtered.size());
    }

    public void add(ProductView view) {
        views.add(view);
    }
}