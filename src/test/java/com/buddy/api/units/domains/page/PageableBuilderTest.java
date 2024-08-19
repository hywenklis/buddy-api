package com.buddy.api.units.domains.page;

import static com.buddy.api.commons.page.PageableBuilder.buildPageable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.buddy.api.commons.page.PageableBuilder;
import com.buddy.api.units.UnitTestAbstract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageableBuilderTest extends UnitTestAbstract {

    @Test
    @DisplayName("Should return default pageable when input is unsorted")
    void should_return_default_Pageable() {
        Pageable input = PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
        Pageable result = buildPageable(input);
        assertDefaultPageable(result);
    }

    @Test
    @DisplayName("Should return same pageable when input has valid custom sort")
    void should_return_same_pageable() {
        Sort customSort = Sort.by("customField").ascending();
        Pageable customPageable =
            PageRequest.of(
                DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE,
                customSort
            );

        Pageable result = buildPageable(customPageable);

        assertThat(result)
            .isEqualTo(customPageable)
            .satisfies(pageable -> {
                assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE_NUMBER);
                assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_PAGE_SIZE);
                assertThat(pageable.getSort().getOrderFor("customField"))
                    .isNotNull()
                    .satisfies(order -> {
                        assertThat(order.getDirection().isAscending()).isTrue();
                        assertThat(order.getProperty()).isEqualTo("customField");
                    });
            });
    }

    private void assertDefaultPageable(final Pageable pageable) {
        assertThat(pageable).isNotNull();
        assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE_NUMBER);
        assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_PAGE_SIZE);
        assertThat(pageable.getSort())
            .isNotNull()
            .satisfies(sort -> sort.forEach(order -> assertThat(order.getDirection())
                .isEqualTo(PageableBuilder.DEFAULT_SORT_DIRECTION)));
    }
}
