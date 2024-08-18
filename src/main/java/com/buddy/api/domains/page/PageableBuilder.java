package com.buddy.api.domains.page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableBuilder {

    public static final String DEFAULT_SORT_PROPERTY = "createDate";
    public static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;

    private PageableBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Pageable buildPageable(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort());
        }
        return pageable;
    }

    private static Sort defaultSort() {
        return Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_PROPERTY);
    }
}
