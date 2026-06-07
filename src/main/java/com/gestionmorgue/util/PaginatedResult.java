package com.gestionmorgue.util;

import java.util.List;

public class PaginatedResult<T> {
    private final List<T> results;
    private final long totalCount;
    private final int page;
    private final int pageSize;
    private final int totalPages;

    public PaginatedResult(List<T> results, long totalCount, int page, int pageSize) {
        this.results = results;
        this.totalCount = totalCount;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
    }

    public List<T> getResults() { return results; }
    public long getTotalCount() { return totalCount; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return Math.max(totalPages, 1); }
    public boolean hasNext() { return page < totalPages - 1; }
    public boolean hasPrevious() { return page > 0; }
}
