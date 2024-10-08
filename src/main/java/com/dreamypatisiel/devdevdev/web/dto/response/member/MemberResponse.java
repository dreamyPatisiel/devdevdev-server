package com.dreamypatisiel.devdevdev.web.dto.response.member;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import lombok.Builder;
import lombok.Data;

@Data
public class MemberResponse {
    private final String name;
    private final Email email;
    private final SocialType socialType;

    @Builder
    private MemberResponse(String name, Email email, SocialType socialType) {
        this.name = name;
        this.email = email;
        this.socialType = socialType;
    }

    public static MemberResponse of(Member member) {
        return MemberResponse.builder()
                .name(member.getName())
                .email(member.getEmail())
                .socialType(member.getSocialType())
                .build();
    }
}
