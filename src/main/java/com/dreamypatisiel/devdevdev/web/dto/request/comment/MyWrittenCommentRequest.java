package com.dreamypatisiel.devdevdev.web.dto.request.comment;

import java.util.Objects;
import lombok.Data;

@Data
public class MyWrittenCommentRequest {
    private Long pickCommentId;
    private Long techCommentId;
    private MyWrittenCommentFilter commentFilter;

    public MyWrittenCommentRequest(Long pickCommentId, Long techCommentId,
                                   MyWrittenCommentFilter myWrittenCommentSort) {
        this.pickCommentId = Objects.requireNonNullElse(pickCommentId, Long.MAX_VALUE);
        this.techCommentId = Objects.requireNonNullElse(techCommentId, Long.MAX_VALUE);
        this.commentFilter = Objects.requireNonNullElse(myWrittenCommentSort, MyWrittenCommentFilter.ALL);
    }
}
