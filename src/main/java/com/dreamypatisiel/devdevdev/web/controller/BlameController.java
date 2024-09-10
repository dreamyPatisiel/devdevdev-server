package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.service.common.MemberBlameService;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "픽픽픽 신고 API", description = "픽픽픽 신고")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class BlameController {

    private final MemberBlameService memberBlameService;

    @Operation(summary = "신고 사유 조회", description = "회원은 신고 사유를 조회합니다.")
    @GetMapping("/blames")
    public ResponseEntity<BasicResponse<BlameTypeResponse>> findBlames() {
        return ResponseEntity.ok(BasicResponse.success(memberBlameService.findBlameType()));
    }
}
