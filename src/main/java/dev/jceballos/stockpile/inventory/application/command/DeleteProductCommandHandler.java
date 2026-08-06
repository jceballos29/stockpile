package dev.jceballos.stockpile.inventory.application.command;

import dev.jceballos.stockpile.inventory.application.port.ProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.ProductNotFoundException;

import java.util.Objects;

public class DeleteProductCommandHandler {

    private final ProductWriteRepository productWriteRepository;

    public DeleteProductCommandHandler(ProductWriteRepository productWriteRepository) {
        this.productWriteRepository = Objects.requireNonNull(productWriteRepository);
    }

    public void handle(DeleteProductCommand command) {
        productWriteRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        productWriteRepository.deleteById(command.productId());
    }
}