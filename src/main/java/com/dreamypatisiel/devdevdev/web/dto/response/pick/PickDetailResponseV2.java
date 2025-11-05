package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import com.dreamypatisiel.devdevdev.web.dto.util.PickResponseUtils;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
public class PickDetailResponseV2 {
    private final String userId;
    private final String nickname;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private final LocalDateTime pickCreatedAt;

    private final String pickTitle;
    private final long voteTotalCount;
    private final long commentTotalCount;
    private final Boolean isAuthor;
    private final Boolean isVoted;
    private final Map<PickOptionType, PickDetailOptionResponse> pickOptions;

    @Builder
    public PickDetailResponseV2(String userId, String nickname, LocalDateTime pickCreatedAt, String pickTitle,
                              long voteTotalCount, long commentTotalCount, boolean isAuthor, boolean isVoted,
                              Map<PickOptionType, PickDetailOptionResponse> pickOptions) {
        this.userId = userId;
        this.nickname = nickname;
        this.pickCreatedAt = pickCreatedAt;
        this.pickTitle = pickTitle;
        this.voteTotalCount = voteTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.isAuthor = isAuthor;
        this.isVoted = isVoted;
        this.pickOptions = pickOptions;
    }

    // 회원 전용
    public static PickDetailResponseV2 of(Pick pick, Member pickMember, Member member,
                                        Map<PickOptionType, PickDetailOptionResponse> pickDetailOptions) {
        return PickDetailResponseV2.builder()
                .userId(CommonResponseUtil.sliceAndMaskEmail(pickMember.getEmail().getEmail()))
                .nickname(pickMember.getNickname().getNickname())
                .pickCreatedAt(pick.getCreatedAt())
                .pickTitle(pick.getTitle().getTitle())
                .voteTotalCount(pick.getVoteTotalCount().getCount())
                .commentTotalCount(pick.getCommentTotalCount().getCount())
                .isAuthor(pick.isEqualMember(member))
                .isVoted(PickResponseUtils.isVotedMember(pick, member))
                .pickOptions(pickDetailOptions)
                .build();
    }

    // 익명 회원 전용
    public static PickDetailResponseV2 of(Pick pick, Member pickMember, AnonymousMember anonymousMember,
                                        Map<PickOptionType, PickDetailOptionResponse> pickDetailOptions) {
        return PickDetailResponseV2.builder()
                .isAuthor(false)
                .pickCreatedAt(pick.getCreatedAt())
                .nickname(pickMember.getNickname().getNickname())
                .userId(CommonResponseUtil.sliceAndMaskEmail(pickMember.getEmail().getEmail()))
                .pickTitle(pick.getTitle().getTitle())
                .voteTotalCount(pick.getVoteTotalCount().getCount())
                .commentTotalCount(pick.getCommentTotalCount().getCount())
                .pickOptions(pickDetailOptions)
                .isVoted(PickResponseUtils.isVotedAnonymousMember(pick, anonymousMember))
                .build();
    }
}

