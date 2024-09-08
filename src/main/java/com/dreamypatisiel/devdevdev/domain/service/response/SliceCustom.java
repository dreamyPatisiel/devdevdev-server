package com.dreamypatisiel.devdevdev.domain.service.response;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

@Getter
public class SliceCustom<T> extends SliceImpl<T> {

    private final long totalElements;

    public SliceCustom(List<T> content, Pageable pageable, boolean hasNext, Long totalElements) {
        super(content, pageable, hasNext);
        this.totalElements = totalElements;
    }
}
