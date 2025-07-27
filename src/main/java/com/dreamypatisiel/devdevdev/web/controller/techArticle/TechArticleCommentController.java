package com.dreamypatisiel.devdevdev.web.controller.techArticle;

import static com.dreamypatisiel.devdevdev.web.WebConstant.HEADER_ANONYMOUS_MEMBER_ID;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleServiceStrategy;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment.TechCommentService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.global.utils.HttpRequestUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.RegisterTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "기술블로그 댓글 API", description = "기술블로그 댓글 작성/수정/삭제, 답글 작성/수정/삭제 API")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class TechArticleCommentController {

    private final TechArticleServiceStrategy techArticleServiceStrategy;

    @Operation(summary = "기술블로그 댓글 작성")
    @PostMapping("/articles/{techArticleId}/comments")
    public ResponseEntity<BasicResponse<TechCommentResponse>> registerMainTechComment(
            @PathVariable Long techArticleId,
            @RequestBody @Validated RegisterTechCommentRequest registerTechCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentService techCommentService = techArticleServiceStrategy.getTechCommentService();
        TechCommentResponse response = techCommentService.registerMainTechComment(techArticleId,
                registerTechCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 답글 작성")
    @PostMapping("/articles/{techArticleId}/comments/{originParentTechCommentId}/{parentTechCommentId}")
    public ResponseEntity<BasicResponse<TechCommentResponse>> registerRepliedTechComment(
            @PathVariable Long techArticleId,
            @PathVariable Long originParentTechCommentId,
            @PathVariable Long parentTechCommentId,
            @RequestBody @Validated RegisterTechCommentRequest registerRepliedTechCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentService techCommentService = techArticleServiceStrategy.getTechCommentService();
        TechCommentResponse response = techCommentService.registerRepliedTechComment(techArticleId,
                originParentTechCommentId,
                parentTechCommentId, registerRepliedTechCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 댓글/답글 수정")
    @PatchMapping("/articles/{techArticleId}/comments/{techCommentId}")
    public ResponseEntity<BasicResponse<TechCommentResponse>> modifyTechComment(
            @PathVariable Long techArticleId,
            @PathVariable Long techCommentId,
            @RequestBody @Validated ModifyTechCommentRequest modifyTechCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentService techCommentService = techArticleServiceStrategy.getTechCommentService();
        TechCommentResponse response = techCommentService.modifyTechComment(techArticleId, techCommentId,
                modifyTechCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 댓글/답글 삭제")
    @DeleteMapping("/articles/{techArticleId}/comments/{techCommentId}")
    public ResponseEntity<BasicResponse<TechCommentResponse>> deleteTechComment(
            @PathVariable Long techArticleId,
            @PathVariable Long techCommentId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentService techCommentService = techArticleServiceStrategy.getTechCommentService();
        TechCommentResponse response = techCommentService.deleteTechComment(techArticleId, techCommentId,
                authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 댓글/답글 조회", description = "기술블로그 댓글/답글을 조회할 수 있습니다.")
    @GetMapping("/articles/{techArticleId}/comments")
    public ResponseEntity<BasicResponse<SliceCustom<TechCommentsResponse>>> getTechComments(
            @PageableDefault(size = 5) Pageable pageable,
            @PathVariable Long techArticleId,
            @RequestParam(required = false) TechCommentSort techCommentSort,
            @RequestParam(required = false) Long techCommentId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        String anonymousMemberId = HttpRequestUtils.getHeaderValue(HEADER_ANONYMOUS_MEMBER_ID);

        TechCommentService techCommentService = techArticleServiceStrategy.getTechCommentService();
        SliceCustom<TechCommentsResponse> response = techCommentService.getTechComments(techArticleId, techCommentId,
                techCommentSort, pageable, anonymousMemberId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 댓글/답글 추천/추천취소")
    @PostMapping("/articles/{techArticleId}/comments/{techCommentId}/recommends")
    public ResponseEntity<BasicResponse<TechCommentRecommendResponse>> recommendTechComment(
            @PathVariable Long techArticleId,
            @PathVariable Long techCommentId
    ) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        TechCommentService techCommentService = techArticleServiceStrategy.getTechCommentService();
        TechCommentRecommendResponse response = techCommentService.recommendTechComment(techArticleId, techCommentId,
                authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기술블로그 베스트 댓글 조회", description = "기술블로그 베스트 댓글을 조회할 수 있습니다.")
    @GetMapping("/articles/{techArticleId}/comments/best")
    public ResponseEntity<BasicResponse<TechCommentsResponse>> getTechBestComments(
            @RequestParam(defaultValue = "3") int size,
            @PathVariable Long techArticleId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        String anonymousMemberId = HttpRequestUtils.getHeaderValue(HEADER_ANONYMOUS_MEMBER_ID);

        TechCommentService techCommentService = techArticleServiceStrategy.getTechCommentService();
        List<TechCommentsResponse> techCommentsResponse = techCommentService.findTechBestComments(size, techArticleId,
                anonymousMemberId, authentication);

        return ResponseEntity.ok(BasicResponse.success(techCommentsResponse));
    }
}
