package com.dreamypatisiel.devdevdev.web.dto.request.comment;

import java.util.Objects;
import lombok.Data;

@Data
public class MyWrittenCommentRequest {
    private Long pickCommentId;
    private Long techCommentId;
    private MyWrittenCommentSort commentSort;

    public MyWrittenCommentRequest(Long pickCommentId, Long techCommentId, MyWrittenCommentSort myWrittenCommentSort) {
        this.pickCommentId = Objects.requireNonNullElse(pickCommentId, Long.MAX_VALUE);
        this.techCommentId = Objects.requireNonNullElse(techCommentId, Long.MAX_VALUE);
        this.commentSort = Objects.requireNonNullElse(myWrittenCommentSort, MyWrittenCommentSort.ALL);
    }
}
