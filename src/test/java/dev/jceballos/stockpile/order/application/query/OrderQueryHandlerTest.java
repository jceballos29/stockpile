package dev.jceballos.stockpile.order.application.query;

import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.InMemoryOrderReadRepository;
import dev.jceballos.stockpile.order.application.port.OrderQuery;
import dev.jceballos.stockpile.order.application.port.OrderView;
import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.order.domain.OrderStatus;
import dev.jceballos.stockpile.shared.Money;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderQueryHandlerTest {

    private InMemoryOrderReadRepository readRepository;
    private OrderQueryHandler handler;

    @BeforeEach
    void setUp() {
        readRepository = new InMemoryOrderReadRepository();
        handler = new OrderQueryHandler(readRepository);
    }

    @Test
    void shouldReturnOrderViewById() {
        OrderId orderId = OrderId.newId();
        readRepository.add(viewOf(orderId, OrderStatus.OPEN));

        OrderView result = handler.getById(orderId);

        assertThat(result.orderId()).isEqualTo(orderId);
    }

    @Test
    void shouldThrowWhenOrderDoesNotExist() {
        assertThatThrownBy(() -> handler.getById(OrderId.newId()))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldFilterByStatus() {
        readRepository.add(viewOf(OrderId.newId(), OrderStatus.OPEN));
        readRepository.add(viewOf(OrderId.newId(), OrderStatus.PAID));
        readRepository.add(viewOf(OrderId.newId(), OrderStatus.PAID));

        var result = handler.list(OrderQuery.byStatus(OrderStatus.PAID, 0, 10));

        assertThat(result.items()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    void shouldPaginateResults() {
        for (int i = 0; i < 5; i++) {
            readRepository.add(viewOf(OrderId.newId(), OrderStatus.OPEN));
        }

        var firstPage = handler.list(OrderQuery.firstPage(2));

        assertThat(firstPage.items()).hasSize(2);
        assertThat(firstPage.totalPages()).isEqualTo(3);
        assertThat(firstPage.hasNext()).isTrue();
    }

    private OrderView viewOf(OrderId orderId, OrderStatus status) {
        return new OrderView(orderId, status, List.of(), Money.of(new BigDecimal("0.00"), "USD"));
    }
}