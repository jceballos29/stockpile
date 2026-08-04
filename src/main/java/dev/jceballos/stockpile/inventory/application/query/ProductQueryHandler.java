package dev.jceballos.stockpile.inventory.application.query;

import dev.jceballos.stockpile.inventory.application.port.ProductQuery;
import dev.jceballos.stockpile.inventory.application.port.ProductReadRepository;
import dev.jceballos.stockpile.inventory.application.port.ProductView;
import dev.jceballos.stockpile.inventory.domain.ProductNotFoundException;
import dev.jceballos.stockpile.shared.PagedResult;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

/**
 * Lado de lectura de CQRS para {@code inventory}: solo depende de
 * {@code ProductReadRepository}, sin ninguna referencia a
 * {@code ProductWriteRepository} -- garantia en tiempo de compilacion de
 * que las consultas no pueden mutar stock ni nada mas.
 */
public class ProductQueryHandler {

    private final ProductReadRepository productReadRepository;

    public ProductQueryHandler(ProductReadRepository productReadRepository) {
        this.productReadRepository = Objects.requireNonNull(productReadRepository);
    }

    /**
     * @param productId el producto a buscar
     * @return el modelo de lectura del producto
     * @throws ProductNotFoundException si no existe
     */
    public ProductView getById(ProductId productId) {
        return productReadRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    /**
     * @param query los criterios de busqueda
     * @return una pagina de resultados
     */
    public PagedResult<ProductView> list(ProductQuery query) {
        return productReadRepository.findAll(query);
    }
}