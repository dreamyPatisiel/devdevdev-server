package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.BookmarkResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechCommentRegisterResponse;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleServiceStrategy;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "기술블로그 API", description = "기술블로그 메인, 상세 API")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class TechArticleController {

    private final TechArticleServiceStrategy techArticleServiceStrategy;

    @Operation(summary = "기술블로그 메인 조회 및 검색")
    @GetMapping("/articles")
    public ResponseEntity<BasicResponse<Slice<TechArticleMainResponse>>> getTechArticles(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) TechArticleSort techArticleSort,
            @RequestParam(required = false) String elasticId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Float score) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        Slice<TechArticleMainResponse> response = techArticleService.getTechArticles(pageable, elasticId,
                techArticleSort, keyword, companyId, score, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 상세 조회")
    @GetMapping("/articles/{techArticleId}")
    public ResponseEntity<BasicResponse<TechArticleDetailResponse>> getTechArticle(@PathVariable Long techArticleId) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        TechArticleDetailResponse response = techArticleService.getTechArticle(techArticleId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 북마크")
    @PostMapping("/articles/{techArticleId}/bookmark")
    public ResponseEntity<BasicResponse<BookmarkResponse>> updateBookmark(@PathVariable Long techArticleId,
                                                                          @RequestParam boolean status) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        BookmarkResponse response = techArticleService.updateBookmark(techArticleId, status, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 댓글 작성")
    @PostMapping("/articles/{techArticleId}/comments")
    public ResponseEntity<BasicResponse<TechCommentRegisterResponse>> registerTechComment(
            @PathVariable Long techArticleId,
            @RequestBody @Validated RegisterTechCommentRequest registerTechCommentRequest) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        TechCommentRegisterResponse response = techArticleService.registerTechComment(techArticleId,
                registerTechCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
