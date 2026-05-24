package com.eventmacha.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Paginated list response wrapper.
 *
 * @param <T> item type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponse<T> {

    private final List<T> items;
    private final int count;
    private final String nextToken;

    private PaginatedResponse(List<T> items, String nextToken) {
        this.items = items;
        this.count = items != null ? items.size() : 0;
        this.nextToken = nextToken;
    }

    public static <T> PaginatedResponse<T> of(List<T> items) {
        return new PaginatedResponse<>(items, null);
    }

    public static <T> PaginatedResponse<T> of(List<T> items, String nextToken) {
        return new PaginatedResponse<>(items, nextToken);
    }

    public List<T> getItems() {
        return items;
    }

    public int getCount() {
        return count;
    }

    public String getNextToken() {
        return nextToken;
    }
}
