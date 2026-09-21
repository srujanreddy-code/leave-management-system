package com.ust.lms.common;

/**
 * Contains default pagination and sorting values used across the application.
 */
public final class PaginationConstants {

    private PaginationConstants() {
    }

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 10;
    public static final String DEFAULT_SORT_DIRECTION = "desc";
    public static final String DEFAULT_SORT_FIELD = "id";
}