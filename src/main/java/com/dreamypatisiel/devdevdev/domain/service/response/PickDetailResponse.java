package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.service.response.util.CommonResponseUtil;
import com.dreamypatisiel.devdevdev.domain.service.response.util.PickResponseUtils;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
public class PickDetailResponse {
    private final String userId;
    private final String nickname;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private final LocalDateTime pickCreatedAt;

    private final String pickTitle;
    private final Boolean isAuthor;
    private final Boolean isVoted;
    private final Map<PickOptionType, PickDetailOptionResponse> pickOptions;

    @Builder
    public PickDetailResponse(String userId, String nickname, LocalDateTime pickCreatedAt, String pickTitle,
                              boolean isAuthor, boolean isVoted,
                              Map<PickOptionType, PickDetailOptionResponse> pickOptions) {
        this.userId = userId;
        this.nickname = nickname;
        this.pickCreatedAt = pickCreatedAt;
        this.pickTitle = pickTitle;
        this.isAuthor = isAuthor;
        this.isVoted = isVoted;
        this.pickOptions = pickOptions;
    }

    // 회원 전용
    public static PickDetailResponse of(Pick pick, Member pickMember, Member member,
                                        Map<PickOptionType, PickDetailOptionResponse> pickDetailOptions) {
        return PickDetailResponse.builder()
                .userId(CommonResponseUtil.sliceAndMaskEmail(pickMember.getEmail().getEmail()))
                .nickname(pickMember.getNickname().getNickname())
                .pickCreatedAt(pick.getCreatedAt())
                .pickTitle(pick.getTitle().getTitle())
                .isAuthor(pick.isEqualMember(member))
                .isVoted(PickResponseUtils.isVotedMember(pick, member))
                .pickOptions(pickDetailOptions)
                .build();
    }

    // 익명 회원 전용
    public static PickDetailResponse of(Pick pick, Member pickMember, AnonymousMember anonymousMember,
                                        Map<PickOptionType, PickDetailOptionResponse> pickDetailOptions) {
        return PickDetailResponse.builder()
                .isAuthor(false)
                .pickCreatedAt(pick.getCreatedAt())
                .nickname(pickMember.getNickname().getNickname())
                .userId(CommonResponseUtil.sliceAndMaskEmail(pickMember.getEmail().getEmail()))
                .pickTitle(pick.getTitle().getTitle())
                .pickOptions(pickDetailOptions)
                .isVoted(PickResponseUtils.isVotedAnonymousMember(pick, anonymousMember))
                .build();
    }
}
