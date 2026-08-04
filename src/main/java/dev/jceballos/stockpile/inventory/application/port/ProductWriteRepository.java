package dev.jceballos.stockpile.inventory.application.port;


import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Optional;

/**
 * Puerto de escritura para el agregado {@code Product}. La implementación
 * real (Fase 6, SQLite) vive en {@code inventory.infrastructure.persistence}.
 */
public interface ProductWriteRepository {

    /**
     * Guarda el estado actual del producto -- inserta si es nuevo, actualiza
     * si ya existía (incluye cambios de stock por `reserve()`/`restore()`).
     *
     * @param product el producto a guardar
     */
    void save(Product product);

    /**
     * Busca un producto por su identidad, para cargarlo, mutarlo (via sus
     * propios métodos de dominio, ej. `reserve()`) y volver a guardarlo.
     *
     * @param productId el identificador del producto
     * @return el producto, si existe
     */
    Optional<Product> findById(ProductId productId);
}