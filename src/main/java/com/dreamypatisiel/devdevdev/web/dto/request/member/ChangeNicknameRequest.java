package com.dreamypatisiel.devdevdev.web.dto.request.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangeNicknameRequest {
    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;
}
