package com.dreamypatisiel.devdevdev.web.controller.pick;

import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickCommentService;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRepliedCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
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

@Tag(name = "픽픽픽 댓글 API", description = "픽픽픽 댓글 작성/수정/삭제, 답글 작성/수정/삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/devdevdev/api/v1")
public class PickCommentController {

    private final MemberPickCommentService memberPickCommentService;

    @Operation(summary = "픽픽픽 댓글 작성", description = "회원은 픽픽픽 댓글을 작성할 수 있습니다.")
    @PostMapping("/picks/{pickId}/comments")
    public ResponseEntity<BasicResponse<PickCommentResponse>> registerPickComment(
            @PathVariable Long pickId,
            @RequestBody @Validated RegisterPickCommentRequest registerPickCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickCommentResponse pickCommentResponse = memberPickCommentService.registerPickComment(pickId,
                registerPickCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(pickCommentResponse));
    }

    @Operation(summary = "픽픽픽 답글 작성", description = "회원은 픽픽픽 댓글에 답글을 작성할 수 있습니다.")
    @PostMapping("/picks/{pickId}/comments/{pickCommentOriginParentId}/{pickCommentParentId}")
    public ResponseEntity<BasicResponse<PickCommentResponse>> registerPickRepliedComment(
            @PathVariable Long pickId,
            @PathVariable Long pickCommentOriginParentId,
            @PathVariable Long pickCommentParentId,
            @RequestBody @Validated RegisterPickRepliedCommentRequest registerPickRepliedCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickCommentResponse pickCommentResponse = memberPickCommentService.registerPickRepliedComment(
                pickCommentParentId, pickCommentOriginParentId, pickId, registerPickRepliedCommentRequest,
                authentication);

        return ResponseEntity.ok(BasicResponse.success(pickCommentResponse));
    }

    @Operation(summary = "픽픽픽 댓글/답글 수정", description = "회원은 자신이 작성한 픽픽픽 댓글/답글을 수정할 수 있습니다.")
    @PatchMapping("/picks/{pickId}/comments/{pickCommentId}")
    public ResponseEntity<BasicResponse<PickCommentResponse>> modifyPickComment(
            @PathVariable Long pickId,
            @PathVariable Long pickCommentId,
            @RequestBody @Validated ModifyPickCommentRequest modifyPickCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickCommentResponse pickCommentResponse = memberPickCommentService.modifyPickComment(pickCommentId, pickId,
                modifyPickCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(pickCommentResponse));
    }

    @Operation(summary = "픽픽픽 댓글/답글 조회", description = "회원은 픽픽픽 댓글/답글을 조회할 수 있습니다.")
    @GetMapping("/picks/{pickId}/comments")
    public ResponseEntity<BasicResponse<SliceCustom<PickCommentsResponse>>> getPickComments(
            @PageableDefault(size = 5, sort = "id", direction = Direction.DESC) Pageable pageable,
            @PathVariable Long pickId,
            @RequestParam(required = false) Long pickCommentId,
            @RequestParam(required = false) PickCommentSort pickCommentSort,
            @RequestParam(required = false) PickOptionType pickOptionType) {

        SliceCustom<PickCommentsResponse> pickCommentsResponse = memberPickCommentService.findPickComments(pageable,
                pickId, pickCommentId, pickCommentSort, pickOptionType);

        return ResponseEntity.ok(BasicResponse.success(pickCommentsResponse));
    }

    @Operation(summary = "픽픽픽 댓글/답글 삭제", description = "회원은 자신이 작성한 픽픽픽 댓글/답글을 삭제할 수 있습니다.(어드민은 모든 댓글 삭제 가능)")
    @DeleteMapping("/picks/{pickId}/comments/{pickCommentId}")
    public ResponseEntity<BasicResponse<PickCommentResponse>> deletePickComment(
            @PathVariable Long pickId,
            @PathVariable Long pickCommentId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickCommentResponse pickCommentResponse = memberPickCommentService.deletePickComment(pickCommentId, pickId,
                authentication);

        return ResponseEntity.ok(BasicResponse.success(pickCommentResponse));
    }

    @Operation(summary = "픽픽픽 댓글/답글 추천/추천 취소", description = "회원은 픽픽픽 댓글/답글에 추천을 할 수 있습니다.(이미 추천한 경우 추천 취소)")
    @PostMapping("/picks/{pickId}/comments/{pickCommentId}/recommends")
    public ResponseEntity<BasicResponse<PickCommentRecommendResponse>> recommendPickComment(
            @PathVariable Long pickId,
            @PathVariable Long pickCommentId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickCommentRecommendResponse pickCommentRecommendResponse = memberPickCommentService.recommendPickComment(
                pickId, pickCommentId, authentication);

        return ResponseEntity.ok(BasicResponse.success(pickCommentRecommendResponse));
    }
}
