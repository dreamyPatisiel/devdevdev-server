package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_ACTION_DELETED_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_REPLY_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_VOTE_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.PickReply;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickReplyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.PickCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickCommentResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickCommentsResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickRepliedCommentsResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickReplyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.SliceCustom;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickReplyRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickRepliedCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickReplyRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberPickCommentService {

    public static final String MODIFY = "수정";
    public static final String REGISTER = "작성";
    public static final String DELETE = "삭제";
    public static final String RECOMMEND = "추천";

    private final TimeProvider timeProvider;
    private final MemberProvider memberProvider;
    private final PickPopularScorePolicy pickPopularScorePolicy;

    private final PickRepository pickRepository;
    private final PickVoteRepository pickVoteRepository;
    private final PickCommentRepository pickCommentRepository;
    private final PickReplyRepository pickReplyRepository;
    private final PickCommentRecommendRepository pickCommentRecommendRepository;

    /**
     * @Note: 픽픽픽 메인 댓글을 작성한다.
     * @Author: 장세웅
     * @Since: 2024.08.23
     */
    @Transactional
    public PickCommentResponse registerPickComment(Long pickId,
                                                   RegisterPickCommentRequest pickMainCommentRequest,
                                                   Authentication authentication) {

        String contents = pickMainCommentRequest.getContents();
        Boolean isPickVotePublic = pickMainCommentRequest.getIsPickVotePublic();

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 조회
        Pick findPick = pickRepository.findById(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));
        // 댓글 갯수 증가 및 인기점수 반영
        findPick.incrementCommentTotalCount();
        findPick.changePopularScore(pickPopularScorePolicy);

        // 픽픽픽 게시글의 승인 상태 검증
        validateIsApprovalPickContentStatus(findPick, INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE, REGISTER);

        // 픽픽픽 선택지 투표 공개인 경우
        if (isPickVotePublic) {
            // 회원이 투표한 픽픽픽 투표 조회
            PickVote findPickVote = pickVoteRepository.findWithPickAndPickOptionByPickIdAndMember(pickId, findMember)
                    .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_VOTE_MESSAGE));

            // 픽픽픽 투표한 픽 옵션의 댓글 작성
            PickComment pickComment = PickComment.createPublicVoteComment(new CommentContents(contents),
                    findMember, findPick, findPickVote);
            pickCommentRepository.save(pickComment);

            return new PickCommentResponse(pickComment.getId());
        }

        // 픽픽픽 선택지 투표 비공개인 경우
        PickComment pickComment = PickComment.createPrivateVoteComment(new CommentContents(contents), findMember,
                findPick);
        pickCommentRepository.save(pickComment);

        return new PickCommentResponse(pickComment.getId());
    }

    /**
     * @Note: 픽픽픽 답글을 작성한다.
     * @Author: 장세웅
     * @Since: 2024.08.24
     */
    @Transactional
    public PickCommentResponse registerPickRepliedComment(Long pickCommentParentId,
                                                          Long pickCommentOriginParentId,
                                                          Long pickId,
                                                          RegisterPickRepliedCommentRequest pickSubCommentRequest,
                                                          Authentication authentication) {

        String contents = pickSubCommentRequest.getContents();

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 답글 대상의 픽픽픽 댓글 조회
        PickComment findParentPickComment = pickCommentRepository.findWithPickByIdAndPickId(pickCommentParentId, pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE));

        // 픽픽픽 게시글의 승인 상태 검증
        Pick findPick = findParentPickComment.getPick();
        validateIsApprovalPickContentStatus(findPick, INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE,
                REGISTER);
        // 댓글 총 갯수 증가 및 인기점수 반영
        findPick.incrementCommentTotalCount();
        findPick.changePopularScore(pickPopularScorePolicy);

        // 픽픽픽 최초 댓글 검증 및 반환
        PickComment findOriginParentPickComment = getAndValidateOriginParentPickComment(
                pickCommentOriginParentId, findParentPickComment);
        // 픽픽픽 최초 댓글의 답글 갯수 증가
        findOriginParentPickComment.incrementReplyTotalCount();

        // 픽픽픽 서브 댓글(답글) 생성
        PickComment pickRepliedComment = PickComment.createRepliedComment(new CommentContents(contents),
                findParentPickComment, findOriginParentPickComment, findMember, findPick);
        pickCommentRepository.save(pickRepliedComment);

        return new PickCommentResponse(pickRepliedComment.getId());
    }

    private PickComment getAndValidateOriginParentPickComment(Long pickCommentOriginParentId,
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

    private void validateIsDeletedPickComment(PickComment pickComment, String message, String messageArgs) {
        if (pickComment.isDeleted()) {
            throw new IllegalArgumentException(String.format(message, messageArgs));
        }
    }

    /**
     * @Note: 회원 자신이 작성한 픽픽픽 댓글/답글을 수정한다. 픽픽픽 공개 여부는 수정할 수 없다.
     * @Author: 장세웅
     * @Since: 2024.08.10
     */
    @Transactional
    public PickCommentResponse modifyPickComment(Long pickCommentId, Long pickId,
                                                 ModifyPickCommentRequest modifyPickCommentRequest,
                                                 Authentication authentication) {

        String contents = modifyPickCommentRequest.getContents();

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 댓글 조회(회원 본인이 댓글 작성, 삭제되지 않은 댓글)
        PickComment findPickComment = pickCommentRepository.findWithPickByIdAndPickIdAndCreatedByIdAndDeletedAtIsNull(
                        pickCommentId, pickId, findMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE));

        // 픽픽픽 게시글의 승인 상태 검증
        validateIsApprovalPickContentStatus(findPickComment.getPick(), INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE,
                MODIFY);

        // 댓글 수정
        findPickComment.changeCommentContents(new CommentContents(contents));

        return new PickCommentResponse(findPickComment.getId());
    }

    /**
     * @Note: 회원 자신이 작성한 픽픽픽 댓글/답글을 삭제한다. 소프트 삭제를 진행한다. 어드민은 모든 댓글/답글을 삭제할 수 있다.
     * @Author: 장세웅
     * @Since: 2024.08.11
     */
    @Transactional
    public PickCommentResponse deletePickComment(Long pickCommentId, Long pickId, Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 어드민은 자신이 작성하지 않은 댓글도 삭제 가능
        if (findMember.isAdmin()) {
            // 픽픽픽 댓글 조회(삭제되지 않은 댓글)
            PickComment findPickComment = pickCommentRepository.findByIdAndPickIdAndDeletedAtIsNull(
                            pickCommentId, pickId)
                    .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE));

            // 소프트 삭제
            findPickComment.changeDeletedAt(timeProvider.getLocalDateTimeNow(), findMember);

            return new PickCommentResponse(findPickComment.getId());
        }

        // 픽픽픽 댓글 조회(회원 본인이 댓글 작성, 삭제되지 않은 댓글)
        PickComment findPickComment = pickCommentRepository.findWithPickByIdAndPickIdAndCreatedByIdAndDeletedAtIsNull(
                        pickCommentId, pickId, findMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE));

        // 픽픽픽 게시글의 승인 상태 검증
        validateIsApprovalPickContentStatus(findPickComment.getPick(), INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE,
                DELETE);

        // 소프트 삭제
        findPickComment.changeDeletedAt(timeProvider.getLocalDateTimeNow(), findMember);

        return new PickCommentResponse(findPickComment.getId());
    }


    /**
     * @Note: 정렬 조건에 따라서 커서 방식으로 픽픽픽 댓글/답글을 조회한다.
     * @Author: 장세웅
     * @Since: 2024.08.25
     */
    public SliceCustom<PickCommentsResponse> findPickComments(Pageable pageable, Long pickId,
                                                              Long pickCommentId, PickCommentSort pickCommentSort,
                                                              PickOptionType pickOptionType) {

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
                .findWithMemberWithPickWithPickVoteByOriginParentIdInAndParentIsNotNullAndOriginParentIsNotNull(
                        originParentIds).stream()
                .collect(Collectors.groupingBy(pickCommentReply -> pickCommentReply.getOriginParent().getId()));

        // 픽픽픽 댓글/답글 응답 생성
        List<PickCommentsResponse> pickCommentsResponse = originParentPickComments.stream()
                .map(originPickComment -> getPickCommentsResponse(originPickComment, pickCommentReplies))
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

    private PickCommentsResponse getPickCommentsResponse(PickComment originPickComment,
                                                         Map<Long, List<PickComment>> pickCommentReplies) {

        // 최상위 댓글 아이디 추출
        Long originPickCommentId = originPickComment.getId();

        // 답글의 최상위 댓글이 존재하면
        if (pickCommentReplies.containsKey(originPickCommentId)) {
            // 답글 만들기
            List<PickRepliedCommentsResponse> pickRepliedComments = getPickRepliedComments(
                    pickCommentReplies, originPickCommentId);

            // 답글이 존재하는 댓글 응답 생성
            return PickCommentsResponse.from(originPickComment, pickRepliedComments);
        }

        // 답글이 없는 댓글 응답 생성
        return PickCommentsResponse.from(originPickComment, Collections.emptyList());
    }

    private List<PickRepliedCommentsResponse> getPickRepliedComments(Map<Long, List<PickComment>> pickCommentReplies,
                                                                     Long originPickCommentId) {
        return pickCommentReplies.get(originPickCommentId).stream()
                .sorted((reply1, reply2) -> reply2.getCreatedAt().compareTo(reply1.getCreatedAt())) // 시간 내림차순으로
                .map(PickRepliedCommentsResponse::from)
                .toList();
    }

    /**
     * @Note: 회원이 픽픽픽 댓글/답글에 추천한다. 이미 추천 상태이면 취소한다.(소프트 삭제)
     * @Author: 장세웅
     * @Since: 2024.09.07
     */
    @Transactional
    public PickCommentRecommendResponse recommendPickComment(Long pickId, Long pickCommendId,
                                                             Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 댓글/답글 조회
        PickComment findPickComment = pickCommentRepository.findWithPickByIdAndPickId(pickCommendId, pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE));

        // 픽픽픽 검증
        validateIsApprovalPickContentStatus(findPickComment.getPick(), INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE,
                RECOMMEND);

        // 픽픽픽 댓글/답글 검증
        validateIsDeletedPickComment(findPickComment, INVALID_CAN_NOT_ACTION_DELETED_PICK_COMMENT_MESSAGE, RECOMMEND);

        return togglePickCommentRecommend(findPickComment, findMember);
    }

    private PickCommentRecommendResponse togglePickCommentRecommend(PickComment pickComment,
                                                                    Member member) {

        // 픽픽픽 댓글/답글 추천 조회
        Optional<PickCommentRecommend> optionalPickCommentRecommend = pickCommentRecommendRepository.findByPickCommentIdAndMemberId(
                pickComment.getId(), member.getId());

        // 댓글/답글에 추천이 존재하면
        if (optionalPickCommentRecommend.isPresent()) {
            // 추천 취소
            PickCommentRecommend pickCommentRecommend = optionalPickCommentRecommend.get();
            pickCommentRecommend.cancelRecommend();

            // 총 추천 갯수 감소
            pickComment.decrementRecommendTotalCount();

            return new PickCommentRecommendResponse(pickCommentRecommend.getRecommendedStatus(),
                    pickComment.getRecommendTotalCount().getCount());
        }

        // 추천
        PickCommentRecommend pickCommentRecommend = PickCommentRecommend.create(pickComment, member);
        pickCommentRecommendRepository.save(pickCommentRecommend);

        // 총 추천 갯수 증가
        pickComment.incrementRecommendTotalCount();

        return new PickCommentRecommendResponse(pickCommentRecommend.getRecommendedStatus(),
                pickComment.getRecommendTotalCount().getCount());
    }

    /**
     * @Note: 픽픽픽 댓글에 답글을 작성한다.
     * @Author: 장세웅
     * @Since: 2024.08.13
     */
    @Deprecated
    @Transactional
    public PickReplyResponse registerPickReply(Long pickCommentId, Long pickId,
                                               RegisterPickReplyRequest registerPickReplyRequest,
                                               Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 댓글 조회
        PickComment findPickComment = pickCommentRepository.findWithPickByIdAndPickId(pickCommentId, pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE));

        // 픽픽픽 댓글이 삭제 상태이면 답글 작성이 불가
        if (findPickComment.isDeleted()) {
            String message = String.format(INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE, REGISTER);
            throw new IllegalArgumentException(message);
        }

        // 픽픽픽 게시글의 승인 상태 검증
        validateIsApprovalPickContentStatus(findPickComment.getPick(), INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE,
                REGISTER);

        // 픽픽픽 답글 작성
        String contents = registerPickReplyRequest.getContents();
        PickReply pickReply = PickReply.create(new CommentContents(contents), findMember, findPickComment);
        pickReplyRepository.save(pickReply);

        return new PickReplyResponse(pickReply.getId());
    }

    /**
     * @Note: 픽픽픽 댓글에 답글을 수정한다.
     * @Author: 장세웅
     * @Since: 2024.08.15
     */
    @Deprecated
    @Transactional
    public PickReplyResponse modifyPickReply(Long pickReplyId, Long pickCommentId, Long pickId,
                                             ModifyPickReplyRequest modifyPickReplyRequest,
                                             Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 답글 조회(픽픽픽, 픽픽픽 댓글 페치 조인)
        PickReply findPickReply = pickReplyRepository.findWithPickWithPickCommentByIdAndPickCommentIdAndPickIdAndCreatedByIdAndDeletedAtIsNull(
                        pickReplyId, pickCommentId, pickId, findMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE));

        // 픽픽픽 게시글의 승인 상태 검증
        validateIsApprovalPickContentStatus(findPickReply.getPickComment().getPick(),
                INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE, MODIFY);

        // 답글 수정
        String contents = modifyPickReplyRequest.getContents();
        findPickReply.changeCommentContents(new CommentContents(contents));

        return new PickReplyResponse(findPickReply.getId());
    }

    /**
     * @Note: 픽픽픽 댓글의 답글을 삭제한다.
     * @Author: 장세웅
     * @Since: 2024.08.18
     */
    @Deprecated
    @Transactional
    public PickReplyResponse deletePickReply(Long pickReplyId, Long pickCommentId, Long pickId,
                                             Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 어드민은 자신이 작성하지 않은 댓글도 삭제 가능
        if (findMember.isAdmin()) {
            // 픽픽픽 답글 조회(삭제되지 않은 답글)
            PickReply findPickReply = pickReplyRepository.findByIdAndPickCommentIdAndPickIdAndDeletedAtIsNull(
                            pickReplyId, pickCommentId, pickId)
                    .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE));

            // 소프트 삭제
            findPickReply.changeDeletedAt(timeProvider.getLocalDateTimeNow(), findMember);

            return new PickReplyResponse(findPickReply.getId());
        }

        // 픽픽픽 답글 조회(픽픽픽, 픽픽픽 댓글 페치 조인)
        PickReply findPickReply = pickReplyRepository.findWithPickWithPickCommentByIdAndPickCommentIdAndPickIdAndCreatedByIdAndDeletedAtIsNull(
                        pickReplyId, pickCommentId, pickId, findMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE));

        // 픽픽픽 게시글의 승인 상태 검증
        validateIsApprovalPickContentStatus(findPickReply.getPickComment().getPick(),
                INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE, DELETE);

        // 소프트 삭제
        findPickReply.changeDeletedAt(timeProvider.getLocalDateTimeNow(), findMember);

        return new PickReplyResponse(findPickReply.getId());
    }

    // 픽픽픽 게시글의 승인 상태 검증
    private void validateIsApprovalPickContentStatus(Pick pick, String message, String messageArgs) {
        if (!pick.isTrueContentStatus(ContentStatus.APPROVAL)) {
            throw new IllegalArgumentException(String.format(message, messageArgs));
        }
    }
}
