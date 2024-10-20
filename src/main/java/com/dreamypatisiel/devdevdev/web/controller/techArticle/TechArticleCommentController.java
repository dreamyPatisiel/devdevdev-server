package com.dreamypatisiel.devdevdev.web.controller.techArticle;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.MemberTechCommentService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.RegisterTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "기술블로그 댓글 API", description = "기술블로그 댓글 작성/수정/삭제, 답글 작성/수정/삭제 API")
@RestController
@RequestMapping("/devdevdev/api/v1/articles")
@RequiredArgsConstructor
public class TechArticleCommentController {

    private final MemberTechCommentService memberTechCommentService;

    @Operation(summary = "기술블로그 댓글 작성")
    @PostMapping("/{techArticleId}/comments")
    public ResponseEntity<BasicResponse<TechCommentResponse>> registerMainTechComment(
            @PathVariable Long techArticleId,
            @RequestBody @Validated RegisterTechCommentRequest registerTechCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentResponse response = memberTechCommentService.registerMainTechComment(techArticleId,
                registerTechCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 답글 작성")
    @PostMapping("/{techArticleId}/comments/{originParentTechCommentId}/{parentTechCommentId}")
    public ResponseEntity<BasicResponse<TechCommentResponse>> registerRepliedTechComment(
            @PathVariable Long techArticleId,
            @PathVariable Long originParentTechCommentId,
            @PathVariable Long parentTechCommentId,
            @RequestBody @Validated RegisterTechCommentRequest registerRepliedTechCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentResponse response = memberTechCommentService.registerRepliedTechComment(techArticleId, originParentTechCommentId,
                parentTechCommentId, registerRepliedTechCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 댓글/답글 수정")
    @PatchMapping("/{techArticleId}/comments/{techCommentId}")
    public ResponseEntity<BasicResponse<TechCommentResponse>> modifyTechComment(
            @PathVariable Long techArticleId,
            @PathVariable Long techCommentId,
            @RequestBody @Validated ModifyTechCommentRequest modifyTechCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentResponse response = memberTechCommentService.modifyTechComment(techArticleId, techCommentId,
                modifyTechCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 댓글/답글 삭제")
    @DeleteMapping("/articles/{techArticleId}/comments/{techCommentId}")
    public ResponseEntity<BasicResponse<TechCommentResponse>> deleteTechComment(
            @PathVariable Long techArticleId,
            @PathVariable Long techCommentId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentResponse response = memberTechCommentService.deleteTechComment(techArticleId, techCommentId,
                authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 댓글/답글 조회")
    @GetMapping("/{techArticleId}/comments")
    public ResponseEntity<BasicResponse<SliceCustom<TechCommentsResponse>>> getTechComments(
            @PageableDefault(size = 5) Pageable pageable,
            @PathVariable Long techArticleId,
            @RequestParam(required = false) TechCommentSort techCommentSort,
            @RequestParam(required = false) Long techCommentId
    ) {

        SliceCustom<TechCommentsResponse> response = memberTechCommentService.getTechComments(techArticleId, techCommentId,
                techCommentSort, pageable);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 댓글/답글 추천/추천취소")
    @PostMapping("/{techArticleId}/comments/{techCommentId}/recommends")
    public ResponseEntity<BasicResponse<TechCommentRecommendResponse>> recommendTechComment(
            @PathVariable Long techArticleId,
            @PathVariable Long techCommentId
    ) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentRecommendResponse response = memberTechCommentService.recommendTechComment(techArticleId, techCommentId,
                authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
