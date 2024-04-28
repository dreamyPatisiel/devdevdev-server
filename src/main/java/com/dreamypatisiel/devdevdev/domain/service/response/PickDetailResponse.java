package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
public class PickDetailResponse {
    private final String username;
    private final String nickname;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime pickCreatedAt;

    private final String pickTitle;
    private final boolean isMemberPick;
    private final Map<PickOptionType, PickDetailOptionResponse> pickOptions;

    @Builder
    public PickDetailResponse(String username, String nickname, LocalDateTime pickCreatedAt, String pickTitle,
                              boolean isMemberPick, Map<PickOptionType, PickDetailOptionResponse> pickOptions) {
        this.username = username;
        this.nickname = nickname;
        this.pickCreatedAt = pickCreatedAt;
        this.pickTitle = pickTitle;
        this.isMemberPick = isMemberPick;
        this.pickOptions = pickOptions;
    }

    public static PickDetailResponse of(Pick findPick, Member member,
                                        Map<PickOptionType, PickDetailOptionResponse> pickDetailOptions) {
        return PickDetailResponse.builder()
                .isMemberPick(findPick.isEqualMember(member))
                .pickCreatedAt(findPick.getCreatedAt())
                .nickname(member.getNickname().getNickname())
                .username(member.getName())
                .pickTitle(findPick.getTitle().getTitle())
                .pickOptions(pickDetailOptions)
                .build();
    }
}
