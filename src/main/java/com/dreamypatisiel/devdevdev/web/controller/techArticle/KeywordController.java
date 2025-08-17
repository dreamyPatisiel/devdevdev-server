package com.dreamypatisiel.devdevdev.web.controller.techArticle;

import com.dreamypatisiel.devdevdev.domain.service.techArticle.keyword.TechKeywordService;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "검색어 자동완성 API", description = "검색어 자동완성, 검색어 추가 API")
@Slf4j
@Profile({"dev", "prod"}) // local 에서는 검색어 자동완성 불가
@RestController
@RequestMapping("/devdevdev/api/v1/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final TechKeywordService techKeywordService;

    @Operation(summary = "기술블로그 검색어 자동완성")
    @GetMapping("/auto-complete")
    public ResponseEntity<BasicResponse<String>> autocompleteKeyword(
            @RequestParam String prefix
    ) {
        List<String> response = techKeywordService.autocompleteKeyword(prefix);
        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
