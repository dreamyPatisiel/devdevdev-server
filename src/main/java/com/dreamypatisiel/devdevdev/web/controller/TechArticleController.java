package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.BookmarkResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleServiceStrategy;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "기술블로그 API", description = "기술블로그 메인, 상세 API")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class TechArticleController {

    private final TechArticleServiceStrategy techArticleServiceStrategy;

    @Operation(summary = "기술블로그 메인 조회 및 검색")
    @GetMapping("/articles")
    public ResponseEntity<BasicResponse<Slice<TechArticleResponse>>> getTechArticles (
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) TechArticleSort techArticleSort,
            @RequestParam(required = false) String elasticId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Float score) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        Slice<TechArticleResponse> response = techArticleService.getTechArticles(pageable, elasticId, techArticleSort, keyword, score, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 상세")
    @GetMapping("/articles/{id}")
    public ResponseEntity<BasicResponse<TechArticleResponse>> getTechArticle (@PathVariable Long id) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        TechArticleResponse response = techArticleService.getTechArticle(id, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 북마크")
    @PostMapping("/articles/{id}/bookmark")
    public ResponseEntity<BasicResponse<BookmarkResponse>> toggleBookmark (@PathVariable Long id) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        BookmarkResponse response = techArticleService.toggleBookmark(id, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
