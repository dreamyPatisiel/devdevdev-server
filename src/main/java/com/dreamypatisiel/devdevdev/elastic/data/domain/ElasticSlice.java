package com.dreamypatisiel.devdevdev.elastic.data.domain;

import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@Getter
public class ElasticSlice<T> extends SliceImpl<T> {
    private final Long totalElements;

    public ElasticSlice(List<T> content, Pageable pageable, Long totalElements, boolean hasNext) {
        super(content, pageable, hasNext);
        this.totalElements = totalElements;
    }
}