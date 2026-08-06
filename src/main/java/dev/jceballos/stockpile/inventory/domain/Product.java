package dev.jceballos.stockpile.inventory.domain;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

public class Product {

    private final ProductId productId;
    private String name;
    private String description;
    private Money price;
    private int stock;

    private Product(ProductId productId, String name, String description, Money price, int stock) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public static Product register(ProductId productId, String name, Money price, int initialStock) {
        return register(productId, name, "", price, initialStock);
    }

    public static Product register(ProductId productId, String name, String description, Money price, int initialStock) {
        Objects.requireNonNull(productId, "El identificador del producto no puede ser nulo");
        Objects.requireNonNull(name, "El nombre del producto no puede ser nulo");
        Objects.requireNonNull(description, "La descripcion no puede ser nula (usar cadena vacia si no hay)");
        Objects.requireNonNull(price, "El precio del producto no puede ser nulo");
        if (name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacio");
        }
        if (initialStock < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }
        return new Product(productId, name, description, price, initialStock);
    }

    public static Product reconstitute(ProductId productId, String name, Money price, int stock) {
        return reconstitute(productId, name, "", price, stock);
    }

    public static Product reconstitute(ProductId productId, String name, String description, Money price, int stock) {
        Objects.requireNonNull(productId, "El identificador del producto no puede ser nulo");
        Objects.requireNonNull(name, "El nombre del producto no puede ser nulo");
        Objects.requireNonNull(description, "La descripcion no puede ser nula");
        Objects.requireNonNull(price, "El precio del producto no puede ser nulo");
        return new Product(productId, name, description, price, stock);
    }

    /**
     * Actualiza nombre, descripcion y precio -- no el stock, que solo se
     * mueve via reserve()/restore().
     */
    public void updateDetails(String name, String description, Money price) {
        Objects.requireNonNull(name, "El nombre del producto no puede ser nulo");
        Objects.requireNonNull(description, "La descripcion no puede ser nula");
        Objects.requireNonNull(price, "El precio del producto no puede ser nulo");
        if (name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacio");
        }
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad a reservar debe ser mayor a cero");
        }
        if (quantity > this.stock) {
            throw new InsufficientStockException(productId, quantity, this.stock);
        }
        this.stock -= quantity;
    }

    public void restore(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad a restituir debe ser mayor a cero");
        }
        this.stock += quantity;
    }

    public ProductId productId() { return productId; }
    public String name() { return name; }
    public String description() { return description; }
    public Money price() { return price; }
    public int stock() { return stock; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Product other)) return false;
        return this.productId.equals(other.productId);
    }

    @Override
    public int hashCode() {
        return productId.hashCode();
    }
}