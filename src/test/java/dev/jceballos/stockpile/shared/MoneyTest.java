package dev.jceballos.stockpile.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

public class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    @DisplayName("Debe crear un Money valido con monto y moneda")
    void shouldCreateValidMoney() {
        Money money = Money.of(new BigDecimal("10.00"), "USD");

        assertThat(money.amount()).isEqualByComparingTo("10.00");
        assertThat(money.currency()).isEqualTo(USD);
    }

    @Test
    @DisplayName("Debe rechazar montos negativos")
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-5.00"), "USD"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Debe sumar dos montos de la misma moneda")
    void shouldAddMoneyWithSameCurrency() {
        Money five = Money.of(new BigDecimal("5.00"), "USD");
        Money ten = Money.of(new BigDecimal("10.00"), "USD");

        Money result = five.add(ten);

        assertThat(result.amount()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("Debe rechazar la suma entre monedas distintas")
    void shouldRejectAdditionOfDifferentCurrencies() {
        Money usd = Money.of(new BigDecimal("5.00"), "USD");
        Money eur = Money.of(new BigDecimal("5.00"), "EUR");

        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Debe multiplicar el monto por na cantidad (total del linea de pedido)")
    void shouldMultiplyByQuantity() {
        Money unitPrice = Money.of(new BigDecimal("9.99"), "USD");

        Money lineTotal = unitPrice.multiply(3);

        assertThat(lineTotal.amount()).isEqualByComparingTo("29.97");
    }

    @Test
    @DisplayName("Debe ser inmutable: add() no modifica la instancia original")
    void shouldBeInmutable() {
        Money original = Money.of(new BigDecimal("10.00"), "USD");

        original.add(Money.of(new BigDecimal("5.00"), "USD"));

        assertThat(original.amount()).isEqualByComparingTo("10.00");
    }

    @Test
    void shouldCreateZeroMoneyForCurrency() {
        Money zero = Money.zero(USD);

        assertThat(zero.amount()).isEqualByComparingTo("0.00");
        assertThat(zero.currency()).isEqualTo(USD);
    }
}
