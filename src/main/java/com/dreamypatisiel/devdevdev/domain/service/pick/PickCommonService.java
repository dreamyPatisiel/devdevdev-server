package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.exception.InternalServerException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.openai.data.response.PickWithSimilarityDto;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingsService;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickRepliedCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickCommonService {

    private static final int SIMILARITY_PICK_MAX_COUNT = 3;

    private final EmbeddingsService embeddingsService;

    protected final PickRepository pickRepository;
    protected final PickCommentRepository pickCommentRepository;
    protected final PickCommentRecommendRepository pickCommentRecommendRepository;

    public List<SimilarPickResponse> findTop3SimilarPicks(Long pickId) {

        // 픽픽픽 조회
        Pick findPick = pickRepository.findById(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 픽픽픽 게시글의 승인 상태가 아니면
        if (!findPick.isTrueContentStatus(ContentStatus.APPROVAL)) {
            throw new IllegalArgumentException(INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE);
        }

        // 임베딩 값이 없으면 500 예외 발생
        if (ObjectUtils.isEmpty(findPick.getEmbeddings())) {
            throw new InternalServerException();
        }

        // 유사도를 계산한 픽픽픽 조회
        List<PickWithSimilarityDto> pickWithSimilarityDto = embeddingsService.getPicksWithSimilarityDtoExcludeTargetPick(
                findPick);

        return pickWithSimilarityDto.stream()
                .map(SimilarPickResponse::from)
                .sorted(Comparator.comparingDouble(SimilarPickResponse::getSimilarity).reversed()) // 내림차순
                .limit(SIMILARITY_PICK_MAX_COUNT)
                .toList();
    }

    // 픽픽픽 게시글의 승인 상태 검증
    public static void validateIsApprovalPickContentStatus(Pick pick, String message,
                                                           @Nullable String messageArgs) {
        if (!pick.isTrueContentStatus(ContentStatus.APPROVAL)) {
            throw new IllegalArgumentException(String.format(message, messageArgs));
        }
    }

    // 픽픽픽 댓글 삭제 상태 검증
    public static void validateIsDeletedPickComment(PickComment pickComment, String message,
                                                    @Nullable String messageArgs) {
        if (pickComment.isDeleted()) {
            throw new IllegalArgumentException(String.format(message, messageArgs));
        }
    }

    /**
     * @Note: 정렬 조건에 따라서 커서 방식으로 픽픽픽 댓글/답글을 조회한다.
     * @Author: 장세웅
     * @Since: 2024.08.25
     */
    public SliceCustom<PickCommentsResponse> findPickComments(Pageable pageable, Long pickId,
                                                              Long pickCommentId, PickCommentSort pickCommentSort,
                                                              PickOptionType pickOptionType, @Nullable Member member) {

        // 픽픽픽 최상위 댓글 조회
        Slice<PickComment> findOriginParentPickComments = pickCommentRepository.findOriginParentPickCommentsByCursor(
                pageable, pickId, pickCommentId, pickCommentSort, pickOptionType);

        // 최상위 댓글 아이디 추출
        List<PickComment> originParentPickComments = findOriginParentPickComments.getContent();
        Set<Long> originParentIds = originParentPickComments.stream()
                .map(PickComment::getId)
                .collect(Collectors.toSet());

        // 픽픽픽 최상위 댓글의 답글 조회(최상위 댓글의 아이디가 key)
        Map<Long, List<PickComment>> pickCommentReplies = pickCommentRepository
                .findWithMemberWithPickWithPickVoteWithPickCommentRecommendsByOriginParentIdInAndParentIsNotNullAndOriginParentIsNotNull(
                        originParentIds).stream()
                .collect(Collectors.groupingBy(pickCommentReply -> pickCommentReply.getOriginParent().getId()));

        // 픽픽픽 댓글/답글 응답 생성
        List<PickCommentsResponse> pickCommentsResponse = originParentPickComments.stream()
                .map(originParentPickComment -> getPickCommentsResponse(member, originParentPickComment,
                        pickCommentReplies))
                .toList();

        // 픽픽픽 최상위 댓글 추출
        PickComment originParentPickComment = findOriginParentPickComments.getContent().stream()
                .findFirst()
                .orElseGet(() -> null);

        // 댓글이 하나도 없으면
        if (ObjectUtils.isEmpty(originParentPickComment)) {
            return new SliceCustom<>(pickCommentsResponse, pageable, false, 0L);
        }

        // 픽픽픽 전체 댓글/답글 갯수 추출
        long originParentPickCommentTotalCount = originParentPickComment.getPick().getCommentTotalCount().getCount();

        return new SliceCustom<>(pickCommentsResponse, pageable, findOriginParentPickComments.hasNext(),
                originParentPickCommentTotalCount);
    }

    private PickCommentsResponse getPickCommentsResponse(Member member, PickComment originPickComment,
                                                         Map<Long, List<PickComment>> pickCommentReplies) {

        // 최상위 댓글 아이디 추출
        Long originPickCommentId = originPickComment.getId();

        // 답글의 최상위 댓글이 존재하면
        if (pickCommentReplies.containsKey(originPickCommentId)) {
            // 답글 만들기
            List<PickRepliedCommentsResponse> pickRepliedComments = getPickRepliedComments(member, pickCommentReplies,
                    originPickCommentId);

            // 답글이 존재하는 댓글 응답 생성
            return PickCommentsResponse.of(member, originPickComment, pickRepliedComments);
        }

        // 답글이 없는 댓글 응답 생성
        return PickCommentsResponse.of(member, originPickComment, Collections.emptyList());
    }

    private List<PickRepliedCommentsResponse> getPickRepliedComments(Member member,
                                                                     Map<Long, List<PickComment>> pickCommentReplies,
                                                                     Long originPickCommentId) {

        return pickCommentReplies.get(originPickCommentId).stream()
                .sorted(Comparator.comparing(PickComment::getCreatedAt)) // 오름차순
                .map(repliedPickComment -> PickRepliedCommentsResponse.of(member, repliedPickComment))
                .toList();

    }

    private Map<Long, List<PickCommentRecommend>> getPickCommentRecommends(Member member, Set<Long> pickCommentIds) {
        // 회원이 존재하지 않으면
        if (ObjectUtils.isEmpty(member)) {
            return Collections.emptyMap();
        }

        // 회원이 추천한 댓글 목록 조회
        return pickCommentRecommendRepository.findByMemberIdAndPickCommentIdIn(
                        member.getId(), pickCommentIds).stream()
                .collect(Collectors.groupingBy(
                        pickCommentRecommend -> pickCommentRecommend.getPickComment().getId()));
    }
}
