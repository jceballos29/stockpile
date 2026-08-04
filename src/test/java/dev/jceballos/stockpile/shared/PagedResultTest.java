package dev.jceballos.stockpile.shared;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PagedResultTest {

    @Test
    void shouldCalculateTotalPagesRoundingUp() {
        PagedResult<String> result = new PagedResult<>(List.of("a", "b"), 0, 2, 5);

        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    void shouldReportHasNextWhenMorePagesRemain() {
        PagedResult<String> result = new PagedResult<>(List.of("a", "b"), 0, 2, 5);

        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void shouldReportNoNextOnLastPage() {
        PagedResult<String> result = new PagedResult<>(List.of("e"), 2, 2, 5);

        assertThat(result.hasNext()).isFalse();
    }
}
