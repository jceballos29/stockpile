package dev.jceballos.stockpile.inventory.domain;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

/**
 * Aggregate Root del contexto {@code inventory}: representa un producto del
 * catálogo, con su precio y su stock disponible.
 * <p>
 * Igual que {@code Order} (contexto {@code order}), NO es un record: tiene
 * identidad propia (dos {@code Product} con el mismo {@code ProductId} son
 * "el mismo producto" aunque su stock difiera) y estado mutable -- el
 * {@code stock} va a cambiar con cada reserva o restitución.
 */
public class Product {
    private final ProductId productId;
    private final String name;
    private final Money price;
    private final int stock;

    private Product(ProductId productId, String name, Money price, int stock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    /**
     * Registra un producto nuevo en el catálogo.
     *
     * @param productId    la identidad del producto
     * @param name         el nombre del producto; no puede estar vacío ni en blanco
     * @param price        el precio unitario
     * @param initialStock el stock inicial; puede ser {@code 0} (agotado desde
     *                     el alta) pero no negativo
     * @return un {@code Product} nuevo con los datos dados
     * @throws NullPointerException     si {@code productId}, {@code name} o {@code price} son nulos
     * @throws IllegalArgumentException si {@code name} esta vacio/en blanco, o si
     *                                  {@code initialStock} es negativo
     */
    public static Product register(ProductId productId, String name, Money price, int initialStock) {
        Objects.requireNonNull(productId, "El identificador del producto no puede ser nulo");
        Objects.requireNonNull(name, "El nombre del producto no puede ser nulo");
        Objects.requireNonNull(price, "El precio del producto no puede ser nulo");
        if (name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        if (initialStock < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }
        return new Product(productId, name, price, initialStock);
    }

    public ProductId productId() {
        return productId;
    }

    public String name() {
        return name;
    }

    public Money price() {
        return price;
    }

    public int stock() {
        return stock;
    }

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
