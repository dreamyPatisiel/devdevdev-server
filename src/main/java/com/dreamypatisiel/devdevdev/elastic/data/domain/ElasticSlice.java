package com.dreamypatisiel.devdevdev.elastic.data.domain;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

@Getter
public class ElasticSlice<T> extends SliceImpl<T> {
    private final long totalElements;

    public ElasticSlice(List<T> content, Pageable pageable, long totalElements, boolean hasNext) {
        super(content, pageable, hasNext);
        this.totalElements = totalElements;
    }
}