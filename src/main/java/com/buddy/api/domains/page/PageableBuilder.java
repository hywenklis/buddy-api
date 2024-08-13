package com.buddy.api.domains.page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableBuilder {

    public static final String DEFAULT_SORT_PROPERTY = "createDate";
    public static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private PageableBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Pageable buildPageable(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, defaultSort());
        }
        return pageable;
    }

    private static Sort defaultSort() {
        return Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_PROPERTY);
    }
}
