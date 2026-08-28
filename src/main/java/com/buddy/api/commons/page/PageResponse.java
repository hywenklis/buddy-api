package com.buddy.api.commons.page;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean isFirst,
    boolean isLast,
    boolean hasNext
) {

    public PageResponse {
        content = content == null ? List.of() : List.copyOf(content);
    }

    @Override
    public List<T> content() {
        return List.copyOf(this.content);
    }

    public static <T> PageResponse<T> of(final Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext()
        );
    }

    public static <T, R> PageResponse<R> of(final Page<T> page, final Function<T, R> mapper) {
        final var mappedContent = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(
            mappedContent,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext()
        );
    }
}
