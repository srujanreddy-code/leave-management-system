package com.ust.lms.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a paginated response containing content and pagination metadata.
 *
 * @param <T> type of the content elements
 */
@Getter
@Setter
public class PageResponseDto<T> {
    private List<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    /**
     * Creates a new PageResponseDto with the specified content and pagination details.
     *
     * @param content       list of content elements
     * @param currentPage   current page number
     * @param pageSize      number of elements per page
     * @param totalElements total number of elements
     * @param totalPages    total number of pages
     */
    public PageResponseDto(List<T> content, int currentPage, int pageSize, long totalElements, int totalPages) {
        this.content = content;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}