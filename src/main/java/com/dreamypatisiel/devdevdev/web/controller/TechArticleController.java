package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleService;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleServiceStrategy;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "기술블로그 API", description = "기술블로그 메인, 상세, 댓글 API")
@Slf4j
@RestController
@RequestMapping("/devdevdev/api/v1/article")
@RequiredArgsConstructor
public class TechArticleController {

    private final TechArticleServiceStrategy techArticleServiceStrategy;


    @Operation(summary = "기술블로그 메인 API")
    @GetMapping("/")
    public ResponseEntity<BasicResponse<Slice<TechArticleResponse>>> getTechArticles(@PageableDefault Pageable pageable,
                                                                                    @RequestParam(required = false) String keyword) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        Slice<TechArticleResponse> response = techArticleService.getTechArticles(pageable, keyword, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
