package com.dreamypatisiel.devdevdev.web.controller.pick;

import com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickCommentService;
import com.dreamypatisiel.devdevdev.domain.service.response.PickCommentResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickReplyResponse;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickReplyRequest;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @Operation(summary = "픽픽픽 댓글 수정", description = "회원은 자신이 작성한 픽픽픽 댓글을 수정할 수 있습니다.")
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

    @Operation(summary = "픽픽픽 댓글 삭제", description = "회원은 자신이 작성한 픽픽픽 댓글을 삭제할 수 있습니다.(어드민은 모든 댓글 삭제 가능)")
    @DeleteMapping("/picks/{pickId}/comments/{pickCommentId}")
    public ResponseEntity<BasicResponse<PickCommentResponse>> deletePickComment(
            @PathVariable Long pickId,
            @PathVariable Long pickCommentId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickCommentResponse pickCommentResponse = memberPickCommentService.deletePickComment(pickCommentId, pickId,
                authentication);

        return ResponseEntity.ok(BasicResponse.success(pickCommentResponse));
    }

    @Operation(summary = "픽픽픽 답글 작성", description = "회원은 픽픽픽 댓글에 답글을 작성할 수 있습니다.")
    @PostMapping("/picks/{pickId}/comments/{pickCommentId}/replies")
    public ResponseEntity<BasicResponse<PickReplyResponse>> registerPickReply(
            @PathVariable Long pickId,
            @PathVariable Long pickCommentId,
            @RequestBody @Validated RegisterPickReplyRequest registerPickReplyRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickReplyResponse pickReplyResponse = memberPickCommentService.registerPickReply(pickCommentId, pickId,
                registerPickReplyRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(pickReplyResponse));
    }
}
