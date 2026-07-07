package com.ekwe_hub.zeeshopserver.shared.api.response;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envelope for a paginated list, nested inside ApiResponse.data.
 * Trimmed down from Spring's Page — only the fields a client actually needs
 * to render pagination controls and know whether more data exists.
 */
@Builder
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
