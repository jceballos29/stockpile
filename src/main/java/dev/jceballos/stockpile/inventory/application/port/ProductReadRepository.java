package dev.jceballos.stockpile.inventory.application.port;

import dev.jceballos.stockpile.shared.PagedResult;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Optional;

/**
 * Puerto de lectura para el agregado {@code Product}. Devuelve
 * {@code ProductView}, nunca el agregado {@code Product} en sí.
 */
public interface ProductReadRepository {

    /**
     * Busca un producto por su identidad, para mostrarlo.
     *
     * @param productId el identificador del producto
     * @return el modelo de lectura del producto, si existe
     */
    Optional<ProductView> findById(ProductId productId);

    /**
     * Lista los productos según los criterios de {@code query} (filtro por
     * nombre, paginación).
     *
     * @param query los criterios de búsqueda
     * @return una página de resultados
     */
    PagedResult<ProductView> findAll(ProductQuery query);
}