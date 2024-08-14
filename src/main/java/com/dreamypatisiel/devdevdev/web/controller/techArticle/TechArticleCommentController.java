package com.dreamypatisiel.devdevdev.web.controller.techArticle;

import com.dreamypatisiel.devdevdev.domain.service.response.TechCommentResponse;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.MemberTechCommentService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.controller.techArticle.request.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.techArticle.request.RegisterTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "기술블로그 댓글 API", description = "기술블로그 댓글 작성/수정/삭제, 답글 작성/수정/삭제 API")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class TechArticleCommentController {

    private final MemberTechCommentService memberTechCommentService;

    @Operation(summary = "기술블로그 댓글 작성")
    @PostMapping("/articles/{techArticleId}/comments")
    public ResponseEntity<BasicResponse<TechCommentResponse>> registerTechComment(
            @PathVariable Long techArticleId,
            @RequestBody @Validated RegisterTechCommentRequest registerTechCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentResponse response = memberTechCommentService.registerTechComment(techArticleId,
                registerTechCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 댓글 수정")
    @PatchMapping("/articles/{techArticleId}/comments/{techCommentId}")
    public ResponseEntity<BasicResponse<TechCommentResponse>> modifyTechComment(
            @PathVariable Long techArticleId,
            @PathVariable Long techCommentId,
            @RequestBody @Validated ModifyTechCommentRequest modifyTechCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentResponse response = memberTechCommentService.modifyTechComment(techArticleId, techCommentId,
                modifyTechCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
