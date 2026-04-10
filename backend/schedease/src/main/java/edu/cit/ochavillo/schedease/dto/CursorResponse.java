package edu.cit.ochavillo.schedease.dto;

import java.util.List;

public record CursorResponse<T>(
        List<T> data,
        Long nextCursor,
        boolean hasMore
) {}
