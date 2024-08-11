package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.TechCommentResponse;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
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

    /**
     * @Note: 기술블로그에 댓글을 작성한다.
     * @Author: 유소영
     * @Since: 2024.08.06
     */
    public TechCommentResponse registerTechComment(Long techArticleId,
                                                   RegisterTechCommentRequest registerTechCommentRequest,
                                                   Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 조회
        TechArticle techArticle = techArticleCommonService.findTechArticle(techArticleId);

        // 댓글 엔티티 생성 및 저장
        String contents = registerTechCommentRequest.getContents();
        TechComment techComment = TechComment.create(new CommentContents(contents), findMember, techArticle);
        techCommentRepository.save(techComment);

        // 데이터 가공
        return TechCommentResponse.from(techComment);
    }

    /**
     * @Note: 기술블로그 댓글을 수정한다.
     * @Author: 유소영
     * @Since: 2024.08.11
     */
    public TechCommentResponse modifyTechComment(Long techArticleId, Long techCommentId,
                                                 ModifyTechCommentRequest modifyTechCommentRequest,
                                                 Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 댓글 조회
        TechComment findTechComment = techCommentRepository.findByIdAndTechArticleIdAndMemberIdAndDeletedAtIsNull(
                        techCommentId, techArticleId, findMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 댓글 수정
        String contents = modifyTechCommentRequest.getContents();
        findTechComment.changeCommentContents(new CommentContents(contents));

        // 데이터 가공
        return TechCommentResponse.from(findTechComment);
    }
}
