package dev.jceballos.stockpile.shared;

import java.util.Objects;

/**
 * Value Object de identidad para un Producto del catálogo de Inventory.
 * <p>
 * A diferencia de un identificador generado internamente (como
 * {@code OrderId}), un {@code ProductId} referencia algo que vive en el
 * contexto {@code inventory} -- el contexto {@code order} nunca crea uno,
 * solo lo recibe y lo usa como referencia.
 * <p>
 * Pertenece al Shared Kernel (ver {@code brief.md}, sección 2).
 *
 * @param value el código del producto (ej. un SKU como {@code "SKU-001"})
 */
public record ProductId(String value) {

    /**
     * Valída que el identificador no sea nulo ni este vacío o en blanco.
     *
     * @throws NullPointerException     si {@code value} es nulo
     * @throws IllegalArgumentException si {@code value} está vacío o compuesto solo por espacios en blanco
     */
    public ProductId {
        Objects.requireNonNull(value, "El identificador del producto no puede ser nulo");
        if (value.isBlank()) {
            throw new IllegalArgumentException("El identificador del producto no puede estar vacío");
        }
    }

    /**
     * Devuelve el código de producto plano, sin el formato técnico de record
     * (por ejemplo, {@code "SKU-001"} en vez de {@code "ProductId[value=SKU-001]"}).
     *
     * @return el valor del identificador
     */
    @Override
    public String toString() {
        return value;
    }
}
