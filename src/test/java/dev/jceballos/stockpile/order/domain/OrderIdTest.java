package dev.jceballos.stockpile.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

public class OrderIdTest {

    @Test
    void shouldGenerateUniqueIds() {
        OrderId first = OrderId.newId();
        OrderId second = OrderId.newId();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldBeEqualByValue() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";

        OrderId first = OrderId.of(uuid);
        OrderId second = OrderId.of(uuid);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> new OrderId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
