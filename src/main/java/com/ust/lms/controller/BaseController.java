package com.ust.lms.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Base controller providing common functionality for REST controllers,
 * including pagination and sorting support.
 */
public class BaseController {
    /**
     * Creates a pageable object using the requested page, page size, sort direction,
     * and sort fields.
     *
     * @param pageNumber    zero-based page number
     * @param pageSize      number of records per page
     * @param sortDirection sorting direction, either ascending or descending
     * @param sort          fields to sort by
     * @return configured pageable object
     */
    protected Pageable getPageable(int pageNumber, int pageSize, String sortDirection, String... sort) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        List<Sort.Order> orders = new ArrayList<>();
        Arrays.stream(sort).toList().forEach(field -> {
            Sort.Order order = new Sort.Order(direction, field);
            orders.add(order);
        });
        return PageRequest.of(pageNumber, pageSize, Sort.by(orders));
    }
}