package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.service.member.MemberService;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleMainResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "내정보 API", description = "내정보 API")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class MypageController {

    private final TechArticleServiceStrategy techArticleServiceStrategy;
    private final MemberService memberService;

    @Operation(summary = "북마크 목록 조회")
    @GetMapping("/mypage/bookmarks")
    public ResponseEntity<BasicResponse<Slice<TechArticleMainResponse>>> getBookmarkedTechArticles(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) BookmarkSort bookmarkSort, // 등록순, 최신순, 조회순
            @RequestParam(required = false) Long techArticleId) {

        TechArticleService techArticleService = techArticleServiceStrategy.getTechArticleService();
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        Slice<TechArticleMainResponse> response = techArticleService.getBookmarkedTechArticles(pageable, techArticleId,
                bookmarkSort, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/mypage/delete")
    public ResponseEntity<BasicResponse<Void>> deleteMember() {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        memberService.deleteMember(authentication);

        return ResponseEntity.ok(BasicResponse.success());
    }
}
