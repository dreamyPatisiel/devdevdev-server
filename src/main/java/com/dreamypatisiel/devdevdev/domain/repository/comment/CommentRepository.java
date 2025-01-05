package com.dreamypatisiel.devdevdev.domain.repository.comment;

import com.dreamypatisiel.devdevdev.domain.repository.comment.mybatis.CommentMapper;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.comment.MyWrittenCommentSort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final CommentMapper commentMapper;
    private final PickCommentRepository pickCommentRepository;
    private final TechCommentRepository techCommentRepository;

    public SliceCustom<MyWrittenCommentDto> findMyWrittenCommentsByCursor(Long memberId,
                                                                          Long pickCommentId,
                                                                          Long techCommentId,
                                                                          MyWrittenCommentSort myWrittenCommentSort,
                                                                          Pageable pageable) {

        // 픽픽픽
        if (MyWrittenCommentSort.PICK.equals(myWrittenCommentSort)) {
            return pickCommentRepository.findMyWrittenPickCommentsByCursor(memberId, pickCommentId, pageable);
        }

        // 기술블로그
        if (MyWrittenCommentSort.TECH_ARTICLE.equals(myWrittenCommentSort)) {
            return techCommentRepository.findMyWrittenTechCommentsByCursor(memberId, techCommentId, pageable);
        }

        // 전체
        // 회원이 작성한 픽픽픽, 기술블로그 댓글 조회
        List<MyWrittenCommentDto> findMyWrittenComments = commentMapper.findByMemberIdAndPickCommentIdAndTechCommentIdOrderByCommentCreatedAtDesc(
                memberId, pickCommentId, techCommentId, pageable.getPageSize());

        // 다음 페이지 존재 여부
        boolean hasNext = findMyWrittenComments.size() >= pageable.getPageSize();

        // 회원이 작성한 댓글 총 갯수(삭제 미포함)
        Long commentTotalCount = countByCreatedByIdAndDeletedAtIsNull(memberId);

        return new SliceCustom<>(findMyWrittenComments, pageable, hasNext, commentTotalCount);
    }

    private Long countByCreatedByIdAndDeletedAtIsNull(Long createdById) {

        // 회원이 작성한 픽픽픽 댓글 갯수(삭제 미포함)
        Long pickCommentTotal = pickCommentRepository.countByCreatedByIdAndDeletedAtIsNull(createdById);

        // 회원이 작성한 기술블로그 댓글 갯수(삭제 미포함)
        Long techCommentTotal = techCommentRepository.countByCreatedByIdAndDeletedAtIsNull(createdById);

        return pickCommentTotal + techCommentTotal;
    }
}
