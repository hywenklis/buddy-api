package com.buddy.api.domains.page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableBuilder {

    public static final String DEFAULT_SORT_PROPERTY = "createDate";

    private PageableBuilder() {}

    public static Pageable buildPageable(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, DEFAULT_SORT_PROPERTY));
        }

        Sort.Order sortOrder = pageable.getSort().iterator().next();
        Sort.Direction direction = sortOrder.getDirection();

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
            Sort.by(direction, DEFAULT_SORT_PROPERTY));
    }
}
