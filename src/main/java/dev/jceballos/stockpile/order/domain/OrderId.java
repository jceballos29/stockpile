package dev.jceballos.stockpile.order.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object de identidad para el agregado Order.
 * <p>
 * Envolver el UUID evita "primitive obsession": sin este tipo, cualquier
 * método que reciba un UUID (de un pedido, de un producto, de cualquier
 * otra cosa futura) se vería idéntico en la firma del método, y el
 * compilador no podría avisarte si mezclaste uno por otro por error.
 *
 * @param value el identificador UUID subyacente
 */
public record OrderId(UUID value) {

    /**
     * @throws NullPointerException si {@code value} es nulo
     */
    public OrderId {
        Objects.requireNonNull(value, "El identificador de la orden no puede ser nulo");
    }

    /**
     * Genera una identidad nueva para un pedido que se está creando ahora.
     *
     * @return un {@code OrderId} nuevo con un UUID aleatorio
     */
    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    /**
     * Reconstruye la identidad de un pedido ya existente a partir de su
     * representación en texto (por ejemplo, al leerlo desde persistencia).
     *
     * @param rawValue el UUID en formato texto (ej. {@code "550e8400-e29b-..."})
     * @return un {@code OrderId} que envuelve ese UUID
     * @throws NullPointerException     si {@code rawValue} es nulo
     * @throws IllegalArgumentException si {@code rawValue} no tiene formato de UUID valido
     */
    public static OrderId of(String rawValue) {
        Objects.requireNonNull(rawValue, "El valor del identificador no puede ser nulo");
        return new OrderId(UUID.fromString(rawValue));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
