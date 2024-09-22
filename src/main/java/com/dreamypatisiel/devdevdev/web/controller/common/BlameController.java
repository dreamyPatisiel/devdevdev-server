package com.dreamypatisiel.devdevdev.web.controller.common;

import com.dreamypatisiel.devdevdev.domain.service.blame.MemberBlameService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.request.common.BlamePathType;
import com.dreamypatisiel.devdevdev.web.dto.request.common.BlameRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "댑댑댑 신고 API", description = "댑댑댑 신고 기능")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class BlameController {

    private final MemberBlameService memberBlameService;

    @Operation(summary = "신고 사유 조회", description = "회원은 신고 사유를 조회합니다.")
    @GetMapping("/blames")
    public ResponseEntity<BasicResponse<BlameTypeResponse>> getBlames() {
        List<BlameTypeResponse> response = memberBlameService.findBlameType();
        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "댑댑댑 신고", description = "회원은 댑댑댑에 사용자가 게시한 게시글(픽픽픽) 또는 댓글을 신고합니다.")
    @PostMapping("/blames/{blamePathType}")
    public ResponseEntity<BasicResponse<BlameResponse>> blame(@PathVariable BlamePathType blamePathType,
                                                              @RequestBody @Validated BlameRequest blameRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        BlameResponse response = memberBlameService.blame(blamePathType, blameRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
