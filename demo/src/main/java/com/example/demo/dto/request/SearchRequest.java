package com.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest<T> {
    private Pagination pagination;
    private Sort sort;
    private T filter;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pagination {
        private Integer page = 1;
        private Integer size = 10;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Sort {
        private String field;
        private String order; // ASC | DESC
    }
}
