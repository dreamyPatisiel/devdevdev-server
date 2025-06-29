package com.dreamypatisiel.devdevdev.web.controller.member;

import com.dreamypatisiel.devdevdev.domain.service.member.MemberNicknameDictionaryService;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class NicknameController {

    private final MemberNicknameDictionaryService memberNicknameDictionaryService;

    @Operation(summary = "랜덤 닉네임 요청", description = "랜덤 닉네임을 생성합니다.")
    @GetMapping("/nickname/random")
    public ResponseEntity<BasicResponse<String>> getRandomNickname() {
        String response = memberNicknameDictionaryService.createRandomNickname();
        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
