package dev.jceballos.stockpile.shared;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProductIdTest {

    @Test
    void shouldBeEqualByValue() {
        Assertions.assertThat(new ProductId("SKU-001")).isEqualTo(new ProductId("SKU-001"));
    }

    @Test
    void shouldRejectBlankValue() {
        Assertions.assertThatThrownBy(() -> new ProductId(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullValue() {
        Assertions.assertThatThrownBy(() -> new ProductId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
