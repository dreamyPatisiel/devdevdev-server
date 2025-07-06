package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickCommentService.REGISTER;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.policy.PickBestCommentsPolicy;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.exception.InternalServerException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.openai.data.response.PickWithSimilarityDto;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingsService;
import com.dreamypatisiel.devdevdev.web.dto.SliceCommentCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickRepliedCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
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
    private final PickBestCommentsPolicy pickBestCommentsPolicy;
    protected final PickPopularScorePolicy pickPopularScorePolicy;

    protected final TimeProvider timeProvider;
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
    protected SliceCommentCustom<PickCommentsResponse> findPickComments(Pageable pageable, Long pickId,
                                                                        Long pickCommentId,
                                                                        PickCommentSort pickCommentSort,
                                                                        EnumSet<PickOptionType> pickOptionTypes,
                                                                        @Nullable Member member) {

        // 픽픽픽 최상위 댓글 조회
        Slice<PickComment> findOriginParentPickComments = pickCommentRepository.findOriginParentPickCommentsByCursor(
                pageable, pickId, pickCommentId, pickCommentSort, pickOptionTypes);

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
            return new SliceCommentCustom<>(pickCommentsResponse, pageable, false, 0L, 0L);
        }

        // 삭제 상태가 아닌 픽픽픽 부모 댓글 조회
        Long pickOriginCommentsIsNotDeleted = pickCommentRepository.countByPickIdAndOriginParentIsNullAndParentIsNullAndDeletedAtIsNull(
                pickId);

        // pickOptionTypes 필터링이 없으면
        if (ObjectUtils.isEmpty(pickOptionTypes)) {
            // 픽픽픽에서 전체 댓글 추출
            Long pickCommentTotalCount = originParentPickComment.getPick().getCommentTotalCount().getCount();

            return new SliceCommentCustom<>(pickCommentsResponse, pageable, findOriginParentPickComments.hasNext(),
                    pickCommentTotalCount, pickOriginCommentsIsNotDeleted);
        }

        // 픽픽픽 댓글/답글 총 갯수
        Long pickCommentTotalCount = getPickCommentTotalCountBy(pickId, pickOptionTypes);

        return new SliceCommentCustom<>(pickCommentsResponse, pageable, findOriginParentPickComments.hasNext(),
                pickCommentTotalCount, pickOriginCommentsIsNotDeleted);
    }

    private Long getPickCommentTotalCountBy(Long pickId, EnumSet<PickOptionType> pickOptionTypes) {
        // 픽픽픽 전체 최상위 댓글 갯수 조회(pickOptionTypes 필터링)
        Set<Long> allOriginParentPickCommentIds = pickCommentRepository.findOriginParentPickCommentsByPickIdAndPickOptionTypeIn(
                        pickId, pickOptionTypes).stream()
                .map(PickComment::getId)
                .collect(Collectors.toSet());

        // 픽픽픽 최상위 댓글의 답글 갯수 조회
        Long childCommentCount = pickCommentRepository.countByOriginParentIdIn(allOriginParentPickCommentIds);

        // 전체 댓글(답글 포함) 갯수
        return allOriginParentPickCommentIds.size() + childCommentCount;
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

    /**
     * @Note: 픽픽픽 베스트 댓글을 조회한다.
     * @Author: 장세웅
     * @Since: 2024.10.09
     */
    protected List<PickCommentsResponse> findPickBestComments(int size, Long pickId, @Nullable Member member) {

        // 베스트 댓글 offset 정책 적용
        int offset = pickBestCommentsPolicy.applySize(size);

        // 베스트 댓글 조회
        List<PickComment> findOriginPickBestComments = pickCommentRepository.findOriginParentPickBestCommentsByPickIdAndOffset(
                pickId, offset);

        // 베스트 댓글 아이디 추출
        Set<Long> originParentIds = findOriginPickBestComments.stream()
                .map(PickComment::getId)
                .collect(Collectors.toSet());

        // 픽픽픽 최상위 댓글의 답글 조회(최상위 댓글의 아이디가 key)
        Map<Long, List<PickComment>> pickBestCommentReplies = pickCommentRepository
                .findWithMemberWithPickWithPickVoteWithPickCommentRecommendsByOriginParentIdInAndParentIsNotNullAndOriginParentIsNotNull(
                        originParentIds).stream()
                .collect(Collectors.groupingBy(pickCommentReply -> pickCommentReply.getOriginParent().getId()));

        // 픽픽픽 댓글/답글 응답 생성
        return findOriginPickBestComments.stream()
                .map(originParentPickComment -> getPickCommentsResponse(member, originParentPickComment,
                        pickBestCommentReplies))
                .toList();
    }

    protected PickComment getAndValidateOriginParentPickComment(Long pickCommentOriginParentId,
                                                                PickComment parentPickComment) {

        // 픽픽픽 답글 대상의 댓글이 삭제 상태이면
        validateIsDeletedPickComment(parentPickComment, INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE, REGISTER);

        // 픽픽픽 답글 대상의 댓글이 최초 댓글이면
        if (parentPickComment.isEqualsId(pickCommentOriginParentId)) {
            return parentPickComment;
        }

        // 픽픽픽 답글 대상의 댓글의 메인 댓글 조회
        PickComment findOriginParentPickComment = pickCommentRepository.findById(pickCommentOriginParentId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE));

        // 픽픽픽 최초 댓글이 삭제 상태이면
        validateIsDeletedPickComment(findOriginParentPickComment, INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE,
                REGISTER);

        return findOriginParentPickComment;
    }

    @Transactional
    protected PickReplyContext prepareForReplyRegistration(Long pickParentCommentId, Long pickCommentOriginParentId,
                                                           Long pickId) {
        // 답글 대상의 픽픽픽 댓글 조회
        PickComment findParentPickComment = pickCommentRepository.findWithPickByIdAndPickId(pickParentCommentId, pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE));

        // 픽픽픽 게시글의 승인 상태 검증
        Pick findPick = findParentPickComment.getPick();
        validateIsApprovalPickContentStatus(findPick, INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE, REGISTER);

        // 댓글 총 갯수 증가 및 인기점수 반영
        findPick.incrementCommentTotalCount();
        findPick.changePopularScore(pickPopularScorePolicy);

        // 픽픽픽 최초 댓글 검증 및 반환
        PickComment findOriginParentPickComment = getAndValidateOriginParentPickComment(
                pickCommentOriginParentId, findParentPickComment);

        // 픽픽픽 최초 댓글의 답글 갯수 증가
        findOriginParentPickComment.incrementReplyTotalCount();

        return new PickReplyContext(findPick, findOriginParentPickComment, findParentPickComment);
    }

    public record PickReplyContext(Pick pick, PickComment originParentPickComment, PickComment parentPickComment) {
    }
}
