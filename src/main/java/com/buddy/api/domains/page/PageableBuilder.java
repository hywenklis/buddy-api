package com.buddy.api.domains.page;

import com.buddy.api.domains.exceptions.InvalidSortFieldException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableBuilder {

    public static final String DEFAULT_SORT_PROPERTY = "createDate";
    public static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private PageableBuilder() {}

    public static Pageable buildPageable(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, defaultSort());
        }
        validateSortFields(pageable.getSort());
        return pageable;
    }

    private static Sort defaultSort() {
        return Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_PROPERTY);
    }

    private static void validateSortFields(Sort sort) {
        sort.stream()
            .filter(order -> !DEFAULT_SORT_PROPERTY.equals(order.getProperty()))
            .filter(order -> !isValidSortDirection(order.getDirection()))
            .findFirst()
            .ifPresent(order -> {
                throw new InvalidSortFieldException(order.getProperty(),
                    "Invalid sort direction: " + order.getDirection());
            });
    }

    private static boolean isValidSortDirection(Sort.Direction direction) {
        return direction != null && (direction.isAscending() || direction.isDescending());
    }
}
