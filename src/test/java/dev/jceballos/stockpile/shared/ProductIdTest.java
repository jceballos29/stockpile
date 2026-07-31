package dev.jceballos.stockpile.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

public class ProductIdTest {

    @Test
    void shouldBeEqualByValue() {
        assertThat(new ProductId("SKU-001")).isEqualTo(new ProductId("SKU-001"));
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> new ProductId(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> new ProductId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
