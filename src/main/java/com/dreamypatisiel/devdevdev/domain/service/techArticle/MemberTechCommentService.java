package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_CAN_NOT_REPLY_DELETED_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.TechCommentResponse;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.controller.techArticle.request.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.techArticle.request.RegisterTechCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberTechCommentService {

    private final TechArticleCommonService techArticleCommonService;
    private final TechCommentRepository techCommentRepository;
    private final MemberProvider memberProvider;
    private final TimeProvider timeProvider;

    /**
     * @Note: 기술블로그에 댓글을 작성한다.
     * @Author: 유소영
     * @Since: 2024.08.06
     */
    @Transactional
    public TechCommentResponse registerMainTechComment(Long techArticleId,
                                                   RegisterTechCommentRequest registerTechCommentRequest,
                                                   Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 조회
        TechArticle techArticle = techArticleCommonService.findTechArticle(techArticleId);

        // 댓글 엔티티 생성 및 저장
        String contents = registerTechCommentRequest.getContents();
        TechComment techComment = TechComment.createMainTechComment(new CommentContents(contents), findMember, techArticle);
        techCommentRepository.save(techComment);

        // 기술블로그 댓글수 증가
        techArticle.incrementCommentCount();

        // 데이터 가공
        return new TechCommentResponse(techComment.getId());
    }

    /**
     * @Note: 기술블로그 댓글에 답글을 작성한다.
     * @Author: 유소영
     * @Since: 2024.08.30
     */
    @Transactional
    public TechCommentResponse registerRepliedTechComment(Long techArticleId,
                                                          Long originParentTechCommentId,
                                                          Long parentTechCommentId,
                                                          RegisterTechCommentRequest registerRepliedTechCommentRequest,
                                                          Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 답글 대상의 기술블로그 댓글 조회
        TechComment findParentTechComment = techCommentRepository.findWithTechArticleByIdAndTechArticleId(
                parentTechCommentId, techArticleId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 삭제된 댓글에는 답글 작성 불가
        if (findParentTechComment.isDeleted()) {
            throw new IllegalArgumentException(INVALID_CAN_NOT_REPLY_DELETED_TECH_COMMENT_MESSAGE);
        }

        // 답글 엔티티 생성 및 저장
        TechComment findOriginParentTechComment = findParentTechComment.getOriginParent() == null? findParentTechComment : findParentTechComment.getOriginParent();
        TechArticle findTechArticle = findParentTechComment.getTechArticle();

        String contents = registerRepliedTechCommentRequest.getContents();
        TechComment repliedTechComment = TechComment.createRepliedTechComment(new CommentContents(contents), findMember,
                findTechArticle, findOriginParentTechComment, findParentTechComment);
        techCommentRepository.save(repliedTechComment);

        // 아티클의 댓글수 증가
        findTechArticle.incrementCommentCount();

        // origin 댓글의 답글수 증가
        findOriginParentTechComment.incrementReplyTotalCount();

        // 데이터 가공
        return new TechCommentResponse(repliedTechComment.getId());
    }

    /**
     * @Note: 기술블로그 댓글을 수정한다. 단, 본인이 작성한 댓글만 수정할 수 있다.
     * @Author: 유소영
     * @Since: 2024.08.11
     */
    @Transactional
    public TechCommentResponse modifyTechComment(Long techArticleId, Long techCommentId,
                                                 ModifyTechCommentRequest modifyTechCommentRequest,
                                                 Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 댓글 조회
        TechComment findTechComment = techCommentRepository.findWithTechArticleByIdAndTechArticleIdAndCreatedByIdAndDeletedAtIsNull(
                        techCommentId, techArticleId, findMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 댓글 수정
        String contents = modifyTechCommentRequest.getContents();
        findTechComment.changeCommentContents(new CommentContents(contents));

        // 데이터 가공
        return new TechCommentResponse(findTechComment.getId());
    }

    /**
     * @Note: 기술블로그 댓글을 삭제한다. 회원은 본인이 작성한 댓글만 삭제할 수 있고, 어드민 권한의 회원은 모든 댓글을 삭제할 수 있다. 삭제는 소프트삭제로 진행한다.
     * @Author: 유소영
     * @Since: 2024.08.13
     */
    @Transactional
    public TechCommentResponse deleteTechComment(Long techArticleId, Long techCommentId,
                                                 Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 어드민 삭제
        if (findMember.isAdmin()) {
            // 기술블로그 댓글 조회
            TechComment findTechComment = techCommentRepository.findWithTechArticleByIdAndTechArticleIdAndDeletedAtIsNull(
                            techCommentId,
                            techArticleId)
                    .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

            // 소프트 삭제
            findTechComment.changeDeletedAt(timeProvider.getLocalDateTimeNow(), findMember);

            // 기술블로그 댓글수 감소
            decrementCommentCount(findTechComment);

            return new TechCommentResponse(findTechComment.getId());
        }

        // 기술블로그 댓글 조회
        TechComment findTechComment = techCommentRepository.findWithTechArticleByIdAndTechArticleIdAndCreatedByIdAndDeletedAtIsNull(
                        techCommentId, techArticleId, findMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 소프트 삭제
        findTechComment.changeDeletedAt(timeProvider.getLocalDateTimeNow(), findMember);

        // 기술블로그 댓글수 감소
        decrementCommentCount(findTechComment);

        // 데이터 가공
        return new TechCommentResponse(findTechComment.getId());
    }

    private static void decrementCommentCount(TechComment findTechComment) {
        // 기술블로그 댓글수 감소
        findTechComment.getTechArticle().decrementCommentCount();

        // 만약 originParent가 존재하면, originParent의 replyTotalCount 감소
        if (findTechComment.getOriginParent() != null) {
            findTechComment.getOriginParent().decrementReplyTotalCount();
        }
    }
}
