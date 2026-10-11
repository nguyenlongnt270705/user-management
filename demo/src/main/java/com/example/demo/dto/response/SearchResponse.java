package com.example.demo.dto.response;

import java.util.List;

public record SearchResponse<T>(List<T> data, long total) {
}
