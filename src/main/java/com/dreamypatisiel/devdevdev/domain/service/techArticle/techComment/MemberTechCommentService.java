package com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_CAN_NOT_RECOMMEND_DELETED_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE;

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
import com.dreamypatisiel.devdevdev.domain.service.techArticle.dto.TechCommentDto;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.TechArticleCommonService;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.dto.SliceCommentCustom;
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
    private final TechCommentRecommendRepository techCommentRecommendRepository;

    public MemberTechCommentService(TechCommentRepository techCommentRepository,
                                    TechArticleCommonService techArticleCommonService, MemberProvider memberProvider,
                                    TimeProvider timeProvider,
                                    TechArticlePopularScorePolicy techArticlePopularScorePolicy,
                                    TechCommentRecommendRepository techCommentRecommendRepository,
                                    TechBestCommentsPolicy techBestCommentsPolicy) {
        super(techCommentRepository, techBestCommentsPolicy, techArticlePopularScorePolicy);
        this.techArticleCommonService = techArticleCommonService;
        this.memberProvider = memberProvider;
        this.timeProvider = timeProvider;
        this.techCommentRecommendRepository = techCommentRecommendRepository;
    }

    /**
     * @Note: 기술블로그에 댓글을 작성한다.
     * @Author: 유소영
     * @Since: 2024.08.06
     */
    @Transactional
    @Override
    public TechCommentResponse registerMainTechComment(Long techArticleId,
                                                       TechCommentDto techCommentDto,
                                                       Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 조회
        TechArticle techArticle = techArticleCommonService.findTechArticle(techArticleId);

        // 댓글 엔티티 생성 및 저장
        String contents = techCommentDto.getContents();
        TechComment techComment = TechComment.createMainTechCommentByMember(new CommentContents(contents), findMember,
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
                                                          TechCommentDto requestedRepliedTechCommentDto,
                                                          Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 답글 대상의 기술블로그 댓글 조회
        TechComment findParentTechComment = techCommentRepository.findWithTechArticleByIdAndTechArticleId(
                        parentTechCommentId, techArticleId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 답글 엔티티 생성 및 저장
        TechComment findOriginParentTechComment = super.getAndValidateOriginParentTechComment(originParentTechCommentId,
                findParentTechComment);
        TechArticle findTechArticle = findParentTechComment.getTechArticle();

        String contents = requestedRepliedTechCommentDto.getContents();
        TechComment repliedTechComment = TechComment.createRepliedTechCommentByMember(new CommentContents(contents), findMember,
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
     * @Note: 기술블로그 댓글을 수정한다. 단, 본인이 작성한 댓글만 수정할 수 있다.
     * @Author: 유소영
     * @Since: 2024.08.11
     */
    @Override
    @Transactional
    public TechCommentResponse modifyTechComment(Long techArticleId, Long techCommentId, TechCommentDto modifyTechCommentDto,
                                                 Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 댓글 조회
        TechComment findTechComment = techCommentRepository.findByIdAndTechArticleIdAndCreatedByIdAndDeletedAtIsNull(
                        techCommentId, techArticleId, findMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 댓글 수정
        String contents = modifyTechCommentDto.getContents();
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
    public SliceCommentCustom<TechCommentsResponse> getTechComments(Long techArticleId, Long techCommentId,
                                                                    TechCommentSort techCommentSort, Pageable pageable,
                                                                    String anonymousMemberId,
                                                                    Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 댓글/답글 조회
        return super.getTechComments(techArticleId, techCommentId, techCommentSort, pageable, findMember, null);
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
                                                           String anonymousMemberId, Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        return super.findTechBestComments(size, techArticleId, findMember, null);
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
