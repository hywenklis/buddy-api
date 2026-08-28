package com.buddy.api.units.commons.page;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.commons.page.PageResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@DisplayName("PageResponse — Unit Tests")
class PageResponseTest {

    @Nested
    @DisplayName("Factory and Constructor Methods")
    class FactoryAndConstructorMethods {

        @Test
        @DisplayName("Should create PageResponse directly from constructor with defensive copy")
        void should_create_page_response_from_constructor() {
            // Arrange
            final var items = List.of("item1", "item2");

            // Act
            final var response = new PageResponse<>(items, 0, 10, 2L, 1, true, true, false);

            // Assert
            assertThat(response.content()).containsExactly("item1", "item2");
            assertThat(response.page()).isZero();
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.totalElements()).isEqualTo(2L);
            assertThat(response.totalPages()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("Should handle null content by setting empty list")
        void should_handle_null_content() {
            // Act
            final var response = new PageResponse<String>(null, 0, 10, 0L, 0, true, true, false);

            // Assert
            assertThat(response.content()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should create PageResponse from Spring Data Page")
        void should_create_from_spring_page() {
            // Arrange
            final var pageable = PageRequest.of(0, 2);
            final var page = new PageImpl<>(List.of("A", "B"), pageable, 5);

            // Act
            final var response = PageResponse.of(page);

            // Assert
            assertThat(response.content()).containsExactly("A", "B");
            assertThat(response.page()).isZero();
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.totalElements()).isEqualTo(5L);
            assertThat(response.totalPages()).isEqualTo(3);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isFalse();
            assertThat(response.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Should create PageResponse from Spring Data Page with mapper function")
        void should_create_from_spring_page_with_mapper() {
            // Arrange
            final var pageable = PageRequest.of(1, 2);
            final var page = new PageImpl<>(List.of(10, 20), pageable, 4);

            // Act
            final var response = PageResponse.of(page, num -> "NUM-" + num);

            // Assert
            assertThat(response.content()).containsExactly("NUM-10", "NUM-20");
            assertThat(response.page()).isEqualTo(1);
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.totalElements()).isEqualTo(4L);
            assertThat(response.totalPages()).isEqualTo(2);
            assertThat(response.isFirst()).isFalse();
            assertThat(response.isLast()).isTrue();
            assertThat(response.hasNext()).isFalse();
        }
    }
}
