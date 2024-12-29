package com.dreamypatisiel.devdevdev.domain.repository.comment.mybatis;

import com.dreamypatisiel.devdevdev.domain.repository.comment.MyWrittenComment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
    List<MyWrittenComment> findByMemberIdAndPickCommentIdAndTechCommentIdOrderByCommentCreatedAtDesc(
            @Param("memberId") Long memberId,
            @Param("pickCommentId") Long pickCommentId,
            @Param("techCommentId") Long techCommentId,
            @Param("limit") int limit);
}
