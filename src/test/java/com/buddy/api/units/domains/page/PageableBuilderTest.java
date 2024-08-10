package com.buddy.api.units.domains.page;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.page.PageableBuilder;
import com.buddy.api.units.UnitTestAbstract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageableBuilderTest extends UnitTestAbstract {

    @Mock
    private Pageable mockPageable;

    @Test
    @DisplayName("Should return default DESC sort when Pageable has empty sort")
    void buildPageable_WithEmptySort_ShouldReturnDefaultDescSort() {
        // Arrange
        int randomPageNumber = 100;
        int randomPageSize = 50;
        arrangePageable(Sort.unsorted(), randomPageNumber, randomPageSize);

        // Act
        Pageable result = PageableBuilder.buildPageable(mockPageable);

        // Assert
        assertPageable(result, Sort.Direction.DESC, randomPageNumber, randomPageSize);
    }

    @Test
    @DisplayName("Should return ASC sort on default property when Pageable has ASC sort")
    void buildPageable_WithAscSort_ShouldReturnAscSortOnDefaultProperty() {
        // Arrange
        int randomPageNumber = 100;
        int randomPageSize = 50;
        Sort ascSort = Sort.by(Sort.Direction.ASC, randomAlphabetic(10));
        arrangePageable(ascSort, randomPageNumber, randomPageSize);

        // Act
        Pageable result = PageableBuilder.buildPageable(mockPageable);

        // Assert
        assertPageable(result, Sort.Direction.ASC, randomPageNumber, randomPageSize);
    }

    @Test
    @DisplayName("Should return DESC sort on default property when Pageable has DESC sort")
    void buildPageable_WithDescSort_ShouldReturnDescSortOnDefaultProperty() {
        // Arrange
        int randomPageNumber = 100;
        int randomPageSize = 50;
        Sort descSort = Sort.by(Sort.Direction.DESC, randomAlphabetic(10));
        arrangePageable(descSort, randomPageNumber, randomPageSize);

        // Act
        Pageable result = PageableBuilder.buildPageable(mockPageable);

        // Assert
        assertPageable(result, Sort.Direction.DESC, randomPageNumber, randomPageSize);
    }

    private void arrangePageable(final Sort sort,
                                 final int pageNumber,
                                 final int pageSize) {
        when(mockPageable.getSort()).thenReturn(sort);
        when(mockPageable.getPageNumber()).thenReturn(pageNumber);
        when(mockPageable.getPageSize()).thenReturn(pageSize);
    }

    private void assertPageable(final Pageable result,
                                final Sort.Direction direction,
                                final int pageNumber,
                                int pageSize) {
        assertThat(result.getSort().getOrderFor(PageableBuilder.DEFAULT_SORT_PROPERTY))
            .isNotNull()
            .satisfies(order -> {
                assertThat(order.getDirection()).isEqualTo(direction);
                assertThat(order.getProperty()).isEqualTo(PageableBuilder.DEFAULT_SORT_PROPERTY);
            });
        assertThat(result.getPageNumber()).isEqualTo(pageNumber);
        assertThat(result.getPageSize()).isEqualTo(pageSize);
    }
}
