package com.dreamypatisiel.devdevdev.domain.service.pick;

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
import com.dreamypatisiel.devdevdev.domain.entity.PickReply;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickReplyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.PickCommentResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickReplyResponse;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickReplyRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickRepliedCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickReplyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberPickCommentService {

    public static final String MODIFY = "수정";
    public static final String REGISTER = "작성";
    public static final String DELETE = "삭제";

    private final TimeProvider timeProvider;
    private final MemberProvider memberProvider;
    private final PickRepository pickRepository;
    private final PickVoteRepository pickVoteRepository;
    private final PickCommentRepository pickCommentRepository;
    private final PickReplyRepository pickReplyRepository;

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

        // 픽픽픽 최초 댓글 검증 및 반환
        PickComment findOriginParentPickComment = getAndValidateOriginParentPickComment(
                pickCommentOriginParentId, findParentPickComment);
        // 픽픽픽 최초 댓글의 답글 갯수 증가
        findOriginParentPickComment.plusOneReplyTotalCount();

        // 픽픽픽 서브 댓글(답글) 생성
        PickComment pickSubComment = PickComment.createRepliedComment(new CommentContents(contents),
                findParentPickComment, findOriginParentPickComment, findMember, findPick);
        pickCommentRepository.save(pickSubComment);

        return new PickCommentResponse(pickSubComment.getId());
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
