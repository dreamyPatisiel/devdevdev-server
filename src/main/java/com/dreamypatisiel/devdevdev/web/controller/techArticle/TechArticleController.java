package com.dreamypatisiel.devdevdev.web.controller.techArticle;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.TechArticleService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleServiceStrategy;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.BookmarkResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleRecommendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.dreamypatisiel.devdevdev.web.WebConstant.HEADER_ANONYMOUS_MEMBER_ID;

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
    public ResponseEntity<BasicResponse<TechArticleDetailResponse>> getTechArticle(
            @PathVariable Long techArticleId,
            @RequestHeader(value = HEADER_ANONYMOUS_MEMBER_ID, required = false) String anonymousMemberId) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        TechArticleDetailResponse response = techArticleService.getTechArticle(techArticleId, anonymousMemberId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 북마크")
    @PostMapping("/articles/{techArticleId}/bookmark")
    public ResponseEntity<BasicResponse<BookmarkResponse>> updateBookmark(@PathVariable Long techArticleId) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        BookmarkResponse response = techArticleService.updateBookmark(techArticleId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 추천")
    @PostMapping("/articles/{techArticleId}/recommend")
    public ResponseEntity<BasicResponse<TechArticleRecommendResponse>> updateRecommend(
            @PathVariable Long techArticleId,
            @RequestHeader(value = HEADER_ANONYMOUS_MEMBER_ID, required = false) String anonymousMemberId
    ) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        TechArticleRecommendResponse response = techArticleService.updateRecommend(techArticleId, anonymousMemberId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
