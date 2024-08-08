package com.dreamypatisiel.devdevdev.web.controller.pick;

import com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickCommentService;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.RegisterPickCommentDto;
import com.dreamypatisiel.devdevdev.domain.service.response.PickCommentResponse;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        RegisterPickCommentDto registerPickCommentDto = RegisterPickCommentDto.of(pickId, registerPickCommentRequest);

        PickCommentResponse pickCommentResponse = memberPickCommentService.registerPickComment(registerPickCommentDto,
                authentication);

        return ResponseEntity.ok(BasicResponse.success(pickCommentResponse));
    }

}
