package dev.jceballos.stockpile.order.domain;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import java.util.Objects;

/**
 * Value Object que representa una línea dentro de un pedido: un producto,
 * la cantidad pedida, y el precio unitario acordado en el momento de
 * agregarlo.
 * <p>
 * Es inmutable. Si la cantidad de un producto en el pedido cambia,
 * {@code Order} reemplaza la instancia entera por una nueva -- no existe
 * un método para "editar" una {@code OrderLine} existente.
 *
 * @param productId referencia al producto (del contexto {@code inventory})
 * @param quantity  la cantidad pedida; debe ser mayor a cero
 * @param unitPrice el precio unitario acordado
 */
public record OrderLine(ProductId productId, int quantity, Money unitPrice) {

    /**
     * @throws NullPointerException     si {@code productId} o {@code unitPrice} son nulos
     * @throws IllegalArgumentException si {@code quantity} no es mayor a cero
     */
    public OrderLine {
        Objects.requireNonNull(productId, "El producto de la linea no puede ser nulo");
        Objects.requireNonNull(unitPrice, "El precio unitario no puede ser nulo");
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }

    /**
     * Calcula el subtotal de esta línea: precio unitario multiplicado por
     * la cantidad.
     *
     * @return un {@code Money} con el subtotal
     */
    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }
}
