package dev.jceballos.stockpile.order.application.port;

import dev.jceballos.stockpile.order.domain.OrderStatus;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderQueryTest {

    @Test
    void shouldCreateFirstPageWithoutFilter() {
        OrderQuery query = OrderQuery.firstPage(10);

        assertThat(query.status()).isEmpty();
        assertThat(query.page()).isZero();
        assertThat(query.pageSize()).isEqualTo(10);
    }

    @Test
    void shouldCreateQueryFilteredByStatus() {
        OrderQuery query = OrderQuery.byStatus(OrderStatus.PAID, 1, 20);

        assertThat(query.status()).contains(OrderStatus.PAID);
        assertThat(query.page()).isEqualTo(1);
    }

    @Test
    void shouldRejectNegativePage() {
        assertThatThrownBy(() -> new OrderQuery(Optional.empty(), -1, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectZeroOrNegativePageSize() {
        assertThatThrownBy(() -> new OrderQuery(Optional.empty(), 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}