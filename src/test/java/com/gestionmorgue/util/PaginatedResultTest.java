package com.gestionmorgue.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaginatedResultTest {

    @Test
    void testPagination() {
        var result = new PaginatedResult<>(java.util.List.of("a", "b", "c"), 10L, 0, 3);
        assertEquals(3, result.getResults().size());
        assertEquals(10, result.getTotalCount());
        assertEquals(0, result.getPage());
        assertEquals(3, result.getPageSize());
        assertEquals(4, result.getTotalPages());
        assertTrue(result.hasNext());
        assertFalse(result.hasPrevious());
    }

    @Test
    void testLastPage() {
        var result = new PaginatedResult<>(java.util.List.of("a"), 10L, 3, 3);
        assertFalse(result.hasNext());
        assertTrue(result.hasPrevious());
    }

    @Test
    void testEmpty() {
        var result = new PaginatedResult<>(java.util.List.of(), 0L, 0, 10);
        assertEquals(1, result.getTotalPages()); // at least 1
        assertFalse(result.hasNext());
    }
}
