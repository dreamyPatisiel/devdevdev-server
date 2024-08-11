package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.TechCommentRegisterResponse;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
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
    public TechCommentRegisterResponse registerTechComment(Long techArticleId,
                                                           RegisterTechCommentRequest registerTechCommentRequest,
                                                           Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 조회
        TechArticle techArticle = techArticleCommonService.findTechArticle(techArticleId);

        // 댓글 엔티티 생성 및 저장
        String contents = registerTechCommentRequest.getContents();
        TechComment techComment = TechComment.create(new CommentContents(contents), member, techArticle);
        techCommentRepository.save(techComment);

        // 데이터 가공
        return TechCommentRegisterResponse.from(techComment);
    }

}
