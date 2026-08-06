package dev.jceballos.stockpile.order.domain;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void shouldCreateEmptyOpenOrder() {
        Order order = Order.open(OrderId.newId(), USD);

        assertThat(order.status()).isEqualTo(OrderStatus.OPEN);
        assertThat(order.lines()).isEmpty();
        assertThat(order.calculateTotal().amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldAddLineWhenStockIsSufficient() {
        Order order = Order.open(OrderId.newId(), USD);
        ProductId laptop = new ProductId("SKU-LAPTOP");

        order.addLine(laptop, 2, Money.of(new BigDecimal("999.00"), "USD"), 5);

        assertThat(order.lines()).hasSize(1);
        assertThat(order.lines().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void shouldRejectAddingLineWhenStockIsInsufficient() {
        Order order = Order.open(OrderId.newId(), USD);
        ProductId laptop = new ProductId("SKU-LAPTOP");

        assertThatThrownBy(() -> order.addLine(laptop, 10, Money.of(new BigDecimal("999.00"), "USD"), 5)).isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void shouldAccumulateQuantityWhenSameProductAddedTwice() {
        Order order = Order.open(OrderId.newId(), USD);
        ProductId laptop = new ProductId("SKU-LAPTOP");
        Money price = Money.of(new BigDecimal("999.00"), "USD");

        order.addLine(laptop, 2, price, 5);
        order.addLine(laptop, 3, price, 5);

        assertThat(order.lines()).hasSize(1);
        assertThat(order.lines().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void shouldRejectWhenAccumulatedQuantityExceedsStockAndKeepPreviousLineIntact() {
        Order order = Order.open(OrderId.newId(), USD);
        ProductId laptop = new ProductId("SKU-LAPTOP");
        Money price = Money.of(new BigDecimal("999.00"), "USD");

        order.addLine(laptop, 3, price, 5);

        assertThatThrownBy(() -> order.addLine(laptop, 3, price, 5)).isInstanceOf(InsufficientStockException.class);

        assertThat(order.lines().get(0).quantity()).isEqualTo(3);
    }

    @Test
    void shouldCalculateTotalAcrossMultipleLines() {
        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        order.addLine(new ProductId("SKU-MOUSE"), 2, Money.of(new BigDecimal("25.00"), "USD"), 10);

        assertThat(order.calculateTotal().amount()).isEqualByComparingTo("1049.00");
    }

    @Test
    void shouldExposeLinesAsUnmodifiableList() {
        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);

        assertThatThrownBy(() -> order.lines().add(null)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldBeEqualByIdentityRegardlessOfContent() {
        OrderId sameId = OrderId.newId();
        Order first = Order.open(sameId, USD);
        Order second = Order.open(sameId, USD);
        second.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void shouldRejectAddingLineWhenOrderIsNotOpen() {
        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        order.pay();

        assertThatThrownBy(() -> order.addLine(new ProductId("SKU-MOUSE"), 1, Money.of(new BigDecimal("25.00"), "USD"), 10)).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldTransitionFromOpenToPaid() {
        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);

        order.pay();

        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldRejectPayingAnEmptyOrder() {
        Order order = Order.open(OrderId.newId(), USD);

        assertThatThrownBy(order::pay).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldRejectPayingAnAlreadyPaidOrder() {
        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        order.pay();

        assertThatThrownBy(order::pay).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldTransitionFromPaidToDispatched() {
        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        order.pay();

        order.dispatch();

        assertThat(order.status()).isEqualTo(OrderStatus.DISPATCHED);
    }

    @Test
    void shouldRejectDispatchingAnOrderThatIsNotPaid() {
        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);

        assertThatThrownBy(order::dispatch).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldRejectDispatchingAnAlreadyDispatchedOrder() {
        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        order.pay();
        order.dispatch();

        assertThatThrownBy(order::dispatch).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldTransitionFromOpenToCancelled() {
        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);

        order.cancel();

        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldAllowCancellingAnEmptyOrder() {
        Order order = Order.open(OrderId.newId(), USD);

        order.cancel();

        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldRejectCancellingAnOrderThatIsNotOpen() {
        Order order = Order.open(OrderId.newId(), USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        order.pay();

        assertThatThrownBy(order::cancel).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldReconstituteOrderWithGivenStatusAndLines() {
        OrderId orderId = OrderId.newId();
        OrderLine line = new OrderLine(new ProductId("SKU-LAPTOP"), 2, Money.of(new BigDecimal("999.00"), "USD"));

        Order order = Order.reconstitute(orderId, USD, OrderStatus.PAID, List.of(line), Instant.now());

        assertThat(order.orderId()).isEqualTo(orderId);
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.lines()).containsExactly(line);
    }

    @Test
    void shouldAllowDispatchingAReconstitutedPaidOrder() {
        Order order = Order.reconstitute(OrderId.newId(), USD, OrderStatus.PAID, List.of(), Instant.now());

        order.dispatch();

        assertThat(order.status()).isEqualTo(OrderStatus.DISPATCHED);
    }

    @Test
    void shouldSetCreatedAtWhenOpeningOrder() {
        Instant before = Instant.now();

        Order order = Order.open(OrderId.newId(), USD);

        Instant after = Instant.now();
        assertThat(order.createdAt()).isBetween(before, after);
    }

    @Test
    void shouldReconstituteOrderWithGivenCreatedAt() {
        Instant createdAt = Instant.parse("2026-01-15T10:30:00Z");

        Order order = Order.reconstitute(OrderId.newId(), USD, OrderStatus.OPEN, List.of(), createdAt);

        assertThat(order.createdAt()).isEqualTo(createdAt);
    }
}
