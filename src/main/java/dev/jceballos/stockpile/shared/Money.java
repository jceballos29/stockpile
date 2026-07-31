package dev.jceballos.stockpile.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value Object que representa una cantidad monetaria: un monto exacto
 * (sin errores de redondeo binario, gracias a {@link BigDecimal}) asociado
 * a una moneda ISO 4217.
 * <p>
 * Es inmutable: toda operación ({@link #add(Money)}, {@link #multiply(int)})
 * devuelve una instancia nueva, nunca modifica la actual.
 * <p>
 * Pertenece al Shared Kernel del proyecto (ver {@code brief.md}): tanto el
 * contexto {@code order} como el contexto {@code inventory} lo usan como
 * vocabulario común.
 *
 * @param amount   el monto; se normaliza automáticamente a 2 decimales
 * @param currency la moneda ISO 4217 asociada a este monto
 */
public record Money(BigDecimal amount, Currency currency) {

    /**
     * Valída las invariantes del monto antes de construir la instancia.
     *
     * @throws NullPointerException     si {@code amount} o {@code currency} son nulos
     * @throws IllegalArgumentException si {@code amount} es negativo
     */
    public Money {
        Objects.requireNonNull(amount, "El monto no puede ser nulo.");
        Objects.requireNonNull(currency, "La moneda no puede ser nula.");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }

        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Crea un {@code Money} a partir de un monto y un código de moneda ISO 4217
     * (por ejemplo, {@code "USD"}).
     *
     * @param amount          el monto
     * @param isoCurrencyCode código de moneda de 3 letras según ISO 4217
     * @return una nueva instancia de {@code Money}
     * @throws IllegalArgumentException si {@code isoCurrencyCode} no es un código ISO valido
     */
    public static Money of(BigDecimal amount, String isoCurrencyCode) {
        return new Money(amount, Currency.getInstance(isoCurrencyCode));
    }

    /**
     * Suma este monto con {@code other}, devolviendo una nueva instancia.
     * No modifica ni este {@code Money} ni {@code other}.
     *
     * @param other el monto a sumar; debe estar en la misma moneda que este
     * @return un nuevo {@code Money} con la suma de ambos montos
     * @throws IllegalArgumentException si {@code other} esta en una moneda distinta
     */
    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Multiplica este monto por {@code quantity}, devolviendo una nueva instancia.
     * Pensado para calcular subtotales (precio unitario por cantidad).
     *
     * @param quantity la cantidad por la que multiplicar
     * @return un nuevo {@code Money} con el resultado
     * @throws IllegalArgumentException si el resultado fuera negativo
     */
    public Money multiply(int quantity) {
        // No validamos "quantity < 0" aca a propósito: si el resultado fuera
        // negativo, el constructor de Money ya lo rechaza. Duplicar el chequeo
        // violaría DRY sobre una regla que ya vive en un solo lugar.
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "No se pueden operar montos en monedas distintas: " + this.currency + " vs " + other.currency
            );
        }
    }
}
