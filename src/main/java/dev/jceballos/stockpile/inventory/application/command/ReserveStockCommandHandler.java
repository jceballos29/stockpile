package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.inventory.application.port.ProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.inventory.domain.ProductNotFoundException;

import java.util.Objects;

/**
 * Orquesta la reserva de stock: carga el producto, le pide que se reserve
 * a si mismo (aplicando su propia invariante), y persiste el resultado.
 */
public class ReserveStockCommandHandler {

    private final ProductWriteRepository productWriteRepository;

    public ReserveStockCommandHandler(ProductWriteRepository productWriteRepository) {
        this.productWriteRepository = Objects.requireNonNull(productWriteRepository);
    }

    /**
     * Ejecuta el comando: carga el producto, reserva la cantidad pedida,
     * y guarda el resultado.
     *
     * @param command el producto y la cantidad a reservar
     * @throws ProductNotFoundException                       si el producto no existe
     * @throws dev.jceballos.stockpile.inventory.domain.InsufficientStockException
     *         si no hay stock suficiente
     */
    public void handle(ReserveStockCommand command) {
        Product product = productWriteRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.reserve(command.quantity());

        productWriteRepository.save(product);
    }
}