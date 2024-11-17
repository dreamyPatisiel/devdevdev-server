package com.dreamypatisiel.devdevdev.web.dto;

import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class SliceCommentCustom<T> extends SliceCustom<T> {

    private final long totalOriginParentComments;

    public SliceCommentCustom(List<T> content, Pageable pageable, boolean hasNext, Long totalElements, long totalOriginParentComments) {
        super(content, pageable, hasNext, totalElements);
        this.totalOriginParentComments = totalOriginParentComments;
    }
}
