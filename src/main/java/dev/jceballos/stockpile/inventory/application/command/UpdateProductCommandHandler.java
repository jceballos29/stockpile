package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.inventory.application.port.ProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.inventory.domain.ProductNotFoundException;

import java.util.Objects;

public class UpdateProductCommandHandler {

    private final ProductWriteRepository productWriteRepository;

    public UpdateProductCommandHandler(ProductWriteRepository productWriteRepository) {
        this.productWriteRepository = Objects.requireNonNull(productWriteRepository);
    }

    public void handle(UpdateProductCommand command) {
        Product product = productWriteRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.updateDetails(command.name(), command.description(), command.price());

        productWriteRepository.save(product);
    }
}