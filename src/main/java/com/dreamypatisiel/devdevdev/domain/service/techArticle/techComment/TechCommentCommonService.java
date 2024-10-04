package com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment;

import com.dreamypatisiel.devdevdev.domain.entity.BasicTime;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechRepliedCommentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechCommentCommonService {

    private final TechCommentRepository techCommentRepository;

    /**
     * @Note: 정렬 조건에 따라 커서 방식으로 기술블로그 댓글 목록을 조회한다.
     * @Author: 유소영
     * @Since: 2024.09.05
     */
    public SliceCustom<TechCommentsResponse> getTechComments(Long techArticleId, Long techCommentId,
                                                             TechCommentSort techCommentSort, Pageable pageable,
                                                             @Nullable Member member) {
        // 기술블로그 최상위 댓글 조회
        Slice<TechComment> findOriginParentTechComments = techCommentRepository.findOriginParentTechCommentsByCursor(
                techArticleId, techCommentId, techCommentSort, pageable);

        // 최상위 댓글 아이디 추출
        List<TechComment> originParentTechComments = findOriginParentTechComments.getContent();
        Set<Long> originParentIds = originParentTechComments.stream()
                .map(TechComment::getId)
                .collect(Collectors.toSet());

        // 최상위 댓글 아이디들의 댓글 답글 조회(최상위 댓글의 아이디가 key)
        Map<Long, List<TechComment>> techCommentReplies = techCommentRepository
                .findWithMemberWithTechArticleByOriginParentIdInAndParentIsNotNullAndOriginParentIsNotNull(
                        originParentIds).stream()
                .collect(Collectors.groupingBy(techCommentReply -> techCommentReply.getOriginParent().getId()));

        // 기술블로그 댓글/답글 응답 생성
        List<TechCommentsResponse> techCommentsResponse = originParentTechComments.stream()
                .map(originParentTechComment -> getTechCommentsResponse(member, originParentTechComment, techCommentReplies))
                .toList();

        // 기술블로그 최상위 댓글 추출하여 댓글 유무 확인
        TechComment firstTechComment = findOriginParentTechComments.getContent().stream()
                .findFirst()
                .orElse(null);

        // 댓글이 하나도 없으면 빈 응답 리턴
        if (ObjectUtils.isEmpty(firstTechComment)) {
            return new SliceCustom<>(techCommentsResponse, pageable, false, 0L);
        }

        // 기술블로그 전체 댓글/답글 개수 추출
        long originTechCommentTotalCount = firstTechComment.getTechArticle().getCommentTotalCount().getCount();

        // 데이터 가공
        return new SliceCustom<>(techCommentsResponse, pageable, findOriginParentTechComments.hasNext(),
                originTechCommentTotalCount);
    }

    private TechCommentsResponse getTechCommentsResponse(Member member, TechComment originParentTechComment,
                                                         Map<Long, List<TechComment>> techCommentReplies) {
        // 최상위 댓글의 아이디 추출
        Long originParentTechCommentId = originParentTechComment.getId();

        // 최상위 댓글의 답글 리스트 추출
        List<TechComment> replies = techCommentReplies.get(originParentTechCommentId);

        if (ObjectUtils.isEmpty(replies)) {
            return TechCommentsResponse.of(member, originParentTechComment, Collections.emptyList());
        }

        // 답글 응답 만들기
        List<TechRepliedCommentsResponse> techRepliedComments = getTechRepliedComments(member, replies);
        return TechCommentsResponse.of(member, originParentTechComment, techRepliedComments);
    }

    private List<TechRepliedCommentsResponse> getTechRepliedComments(Member member, List<TechComment> replies) {
        return replies.stream()
                .sorted(Comparator.comparing(BasicTime::getCreatedAt))
                .map(repliedTechComment -> TechRepliedCommentsResponse.of(member, repliedTechComment))
                .toList();
    }
}
