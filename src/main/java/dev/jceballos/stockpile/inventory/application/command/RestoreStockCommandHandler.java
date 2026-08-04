package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.inventory.application.port.ProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.inventory.domain.ProductNotFoundException;

import java.util.Objects;

/**
 * Orquesta la restitución de stock: carga el producto, le pide que
 * restituya la cantidad, y persiste el resultado.
 */
public class RestoreStockCommandHandler {

    private final ProductWriteRepository productWriteRepository;

    public RestoreStockCommandHandler(ProductWriteRepository productWriteRepository) {
        this.productWriteRepository = Objects.requireNonNull(productWriteRepository);
    }

    /**
     * Ejecuta el comando: carga el producto, restituye la cantidad pedida,
     * y guarda el resultado.
     *
     * @param command el producto y la cantidad a restituir
     * @throws ProductNotFoundException si el producto no existe
     */
    public void handle(RestoreStockCommand command) {
        Product product = productWriteRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.restore(command.quantity());

        productWriteRepository.save(product);
    }
}