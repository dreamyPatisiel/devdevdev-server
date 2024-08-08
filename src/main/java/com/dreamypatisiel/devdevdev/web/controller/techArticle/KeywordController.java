package com.dreamypatisiel.devdevdev.web.controller.techArticle;

import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticKeywordService;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "검색어 자동완성 API", description = "검색어 자동완성, 검색어 추가 API")
@Slf4j
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class KeywordController {

    private final ElasticKeywordService elasticKeywordService;

    @Operation(summary = "기술블로그 검색어 자동완성")
    @GetMapping("/keywords/auto-complete")
    public ResponseEntity<BasicResponse<String>> autocompleteKeyword(@RequestParam String prefix)
            throws IOException {

        List<String> response = elasticKeywordService.autocompleteKeyword(prefix);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
