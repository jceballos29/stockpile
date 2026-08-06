package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.inventory.application.port.ProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;

import java.util.Objects;

/**
 * Orquesta el registro de un producto nuevo: construye el agregado
 * {@code Product} y lo persiste.
 */
public class RegisterProductCommandHandler {

    private final ProductWriteRepository productWriteRepository;

    public RegisterProductCommandHandler(ProductWriteRepository productWriteRepository) {
        this.productWriteRepository = Objects.requireNonNull(productWriteRepository);
    }

    /**
     * Ejecuta el comando: registra el producto y lo guarda.
     *
     * @param command los datos del producto a registrar
     */
    public void handle(RegisterProductCommand command) {
        Product product = Product.register(command.productId(), command.name(), command.description(), command.price(), command.initialStock());
        productWriteRepository.save(product);
    }
}