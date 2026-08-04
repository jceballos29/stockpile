package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.InMemoryOrderWriteRepository;
import dev.jceballos.stockpile.order.domain.InvalidOrderStateException;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.order.domain.OrderStatus;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayOrderCommandHandlerTest {

    private static final Currency USD = Currency.getInstance("USD");

    private InMemoryOrderWriteRepository orderRepository;
    private PayOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderWriteRepository();
        handler = new PayOrderCommandHandler(orderRepository);
    }

    @Test
    void shouldPayOrderAndPersistTheChange() {
        OrderId orderId = OrderId.newId();
        Order order = Order.open(orderId, USD);
        order.addLine(new ProductId("SKU-LAPTOP"), 1, Money.of(new BigDecimal("999.00"), "USD"), 5);
        orderRepository.save(order);

        handler.handle(new PayOrderCommand(orderId));

        assertThat(orderRepository.findById(orderId).orElseThrow().status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldThrowWhenOrderDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new PayOrderCommand(OrderId.newId())))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowWhenOrderHasNoLines() {
        OrderId orderId = OrderId.newId();
        orderRepository.save(Order.open(orderId, USD));

        assertThatThrownBy(() -> handler.handle(new PayOrderCommand(orderId)))
                .isInstanceOf(InvalidOrderStateException.class);
    }
}