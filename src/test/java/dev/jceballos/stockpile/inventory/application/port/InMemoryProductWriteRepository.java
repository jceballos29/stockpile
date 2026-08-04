package dev.jceballos.stockpile.inventory.application.port;


import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria de {@code ProductWriteRepository}, para testear
 * handlers de aplicación sin depender de SQLite. Escrita a mano, no con un
 * framework de mocking -- decision del brief desde el día uno.
 */
public class InMemoryProductWriteRepository implements ProductWriteRepository {

    private final Map<ProductId, Product> storage = new HashMap<>();

    @Override
    public void save(Product product) {
        storage.put(product.productId(), product);
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        return Optional.ofNullable(storage.get(productId));
    }
}