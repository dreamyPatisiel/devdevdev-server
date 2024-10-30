package com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_CAN_NOT_RECOMMEND_DELETED_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_CAN_NOT_REPLY_DELETED_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.TechArticleCommonService.validateIsDeletedTechComment;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.TechCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.policy.TechBestCommentsPolicy;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.TechArticleCommonService;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.RegisterTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentsResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberTechCommentService extends TechCommentCommonService implements TechCommentService {

    private final TechArticleCommonService techArticleCommonService;
    private final MemberProvider memberProvider;
    private final TimeProvider timeProvider;
    private final TechArticlePopularScorePolicy techArticlePopularScorePolicy;

    private final TechCommentRepository techCommentRepository;
    private final TechCommentRecommendRepository techCommentRecommendRepository;

    public MemberTechCommentService(TechCommentRepository techCommentRepository,
                                    TechArticleCommonService techArticleCommonService, MemberProvider memberProvider,
                                    TimeProvider timeProvider,
                                    TechArticlePopularScorePolicy techArticlePopularScorePolicy,
                                    TechCommentRepository techCommentRepository1,
                                    TechCommentRecommendRepository techCommentRecommendRepository,
                                    TechBestCommentsPolicy techBestCommentsPolicy) {
        super(techCommentRepository, techBestCommentsPolicy);
        this.techArticleCommonService = techArticleCommonService;
        this.memberProvider = memberProvider;
        this.timeProvider = timeProvider;
        this.techArticlePopularScorePolicy = techArticlePopularScorePolicy;
        this.techCommentRepository = techCommentRepository1;
        this.techCommentRecommendRepository = techCommentRecommendRepository;
    }

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
        TechComment techComment = TechComment.createMainTechComment(new CommentContents(contents), findMember,
                techArticle);
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

        // 답글 엔티티 생성 및 저장
        TechComment findOriginParentTechComment = getAndValidateOriginParentTechComment(originParentTechCommentId,
                findParentTechComment);
        TechArticle findTechArticle = findParentTechComment.getTechArticle();

        String contents = registerRepliedTechCommentRequest.getContents();
        TechComment repliedTechComment = TechComment.createRepliedTechComment(new CommentContents(contents), findMember,
                findTechArticle, findOriginParentTechComment, findParentTechComment);
        techCommentRepository.save(repliedTechComment);

        // 아티클의 댓글수 증가
        findTechArticle.incrementCommentCount();
        findTechArticle.changePopularScore(techArticlePopularScorePolicy);

        // origin 댓글의 답글수 증가
        findOriginParentTechComment.incrementReplyTotalCount();

        // 데이터 가공
        return new TechCommentResponse(repliedTechComment.getId());
    }

    /**
     * @Note: 답글 대상의 댓글을 조회하고, 답글 대상의 댓글이 최초 댓글이면 답글 대상으로 반환한다.
     * @Author: 유소영
     * @Since: 2024.09.06
     */
    private TechComment getAndValidateOriginParentTechComment(Long originParentTechCommentId,
                                                              TechComment parentTechComment) {

        // 삭제된 댓글에는 답글 작성 불가
        validateIsDeletedTechComment(parentTechComment, INVALID_CAN_NOT_REPLY_DELETED_TECH_COMMENT_MESSAGE, null);

        // 답글 대상의 댓글이 최초 댓글이면 답글 대상으로 반환
        if (parentTechComment.isEqualsId(originParentTechCommentId)) {
            return parentTechComment;
        }

        // 답글 대상의 댓글의 메인 댓글 조회
        TechComment findOriginParentTechComment = techCommentRepository.findById(originParentTechCommentId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 최초 댓글이 삭제 상태이면 답글 작성 불가
        validateIsDeletedTechComment(findOriginParentTechComment, INVALID_CAN_NOT_REPLY_DELETED_TECH_COMMENT_MESSAGE,
                null);

        return findOriginParentTechComment;
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
        TechComment findTechComment = techCommentRepository.findByIdAndTechArticleIdAndCreatedByIdAndDeletedAtIsNull(
                        techCommentId, techArticleId, findMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 댓글 수정
        String contents = modifyTechCommentRequest.getContents();
        findTechComment.modifyCommentContents(new CommentContents(contents), timeProvider.getLocalDateTimeNow());

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
            TechComment findTechComment = techCommentRepository.findByIdAndTechArticleIdAndDeletedAtIsNull(
                            techCommentId,
                            techArticleId)
                    .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

            // 소프트 삭제
            findTechComment.changeDeletedAt(timeProvider.getLocalDateTimeNow(), findMember);

            return new TechCommentResponse(findTechComment.getId());
        }

        // 기술블로그 댓글 조회
        TechComment findTechComment = techCommentRepository.findByIdAndTechArticleIdAndCreatedByIdAndDeletedAtIsNull(
                        techCommentId, techArticleId, findMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 소프트 삭제
        findTechComment.changeDeletedAt(timeProvider.getLocalDateTimeNow(), findMember);

        // 데이터 가공
        return new TechCommentResponse(findTechComment.getId());
    }

    /**
     * @Note: 정렬 조건에 따라 커서 방식으로 기술블로그 댓글 목록을 조회한다.
     * @Author: 유소영
     * @Since: 2024.09.05
     */
    public SliceCustom<TechCommentsResponse> getTechComments(Long techArticleId, Long techCommentId,
                                                             TechCommentSort techCommentSort, Pageable pageable,
                                                             Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 댓글/답글 조회
        return super.getTechComments(techArticleId, techCommentId, techCommentSort, pageable, findMember);
    }

    /**
     * @Note: 기술블로그 댓글을 추천하거나 추천을 취소한다.
     * @Author: 유소영
     * @Since: 2024.09.13
     */
    @Transactional
    public TechCommentRecommendResponse recommendTechComment(Long techArticleId, Long techCommentId,
                                                             Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 댓글/답글 조회
        TechComment findTechComment = techCommentRepository.findWithTechArticleByIdAndTechArticleId(techCommentId,
                        techArticleId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 삭제된 댓글에는 답글 작성 불가
        if (findTechComment.isDeleted()) {
            throw new IllegalArgumentException(INVALID_CAN_NOT_RECOMMEND_DELETED_TECH_COMMENT_MESSAGE);
        }

        return toggleTechCommentRecommend(findTechComment, findMember);
    }

    /**
     * @Note: 회원이 기술블로그 베스트 댓글을 조회한다.
     * @Author: 장세웅
     * @Since: 2024.10.27
     */
    @Override
    public List<TechCommentsResponse> findTechBestComments(int size, Long techArticleId,
                                                           Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        return super.findTechBestComments(size, techArticleId, findMember);
    }

    private TechCommentRecommendResponse toggleTechCommentRecommend(TechComment techComment, Member member) {

        // 기술블로그 댓글/답글 추천 조회
        Optional<TechCommentRecommend> optionalTechCommentRecommend = techCommentRecommendRepository
                .findByTechCommentIdAndMemberId(techComment.getId(), member.getId());

        // 댓글/답글에 추천이 존재하면 toggle
        if (optionalTechCommentRecommend.isPresent()) {
            TechCommentRecommend techCommentRecommend = optionalTechCommentRecommend.get();

            // 추천 상태이면 취소
            if (techCommentRecommend.isRecommended()) {
                techCommentRecommend.cancelRecommend();
                techComment.decrementRecommendTotalCount();

                return new TechCommentRecommendResponse(techCommentRecommend.getRecommendedStatus(),
                        techComment.getRecommendTotalCount().getCount());
            }

            // 추천 상태가 아니라면 추천
            techCommentRecommend.recommend();
            techComment.incrementRecommendTotalCount();

            return new TechCommentRecommendResponse(techCommentRecommend.getRecommendedStatus(),
                    techComment.getRecommendTotalCount().getCount());
        }

        // 추천 생성
        TechCommentRecommend techCommentRecommend = TechCommentRecommend.create(techComment, member);
        techCommentRecommendRepository.save(techCommentRecommend);

        // 총 추천 갯수 증가
        techComment.incrementRecommendTotalCount();

        return new TechCommentRecommendResponse(techCommentRecommend.getRecommendedStatus(),
                techComment.getRecommendTotalCount().getCount());
    }
}
