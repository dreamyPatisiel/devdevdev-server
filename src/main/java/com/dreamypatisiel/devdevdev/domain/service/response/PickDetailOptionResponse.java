package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.service.response.util.PickResponseUtils;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class PickDetailOptionResponse {
    private final Long id;
    private final String title;
    private final Boolean isPicked;
    private final int percent;
    private final String content;
    private final long voteTotalCount;
    private final List<PickDetailOptionImage> pickDetailOptionImages;

    @Builder
    public PickDetailOptionResponse(Long id, String title, boolean isPicked, BigDecimal percent, String content,
                                    long voteTotalCount, List<PickDetailOptionImage> pickDetailOptionImages) {
        this.id = id;
        this.title = title;
        this.isPicked = isPicked;
        this.percent = percent.intValueExact();
        this.content = content;
        this.voteTotalCount = voteTotalCount;
        this.pickDetailOptionImages = pickDetailOptionImages;
    }

    // 회원 전용
    public static PickDetailOptionResponse of(PickOption pickOption, Pick findPick, Member member) {
        return PickDetailOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle().getTitle())
                .isPicked(PickResponseUtils.isPickedPickOptionByMember(findPick, pickOption, member))
                .percent(PickOption.calculatePercentBy(findPick, pickOption))
                .voteTotalCount(pickOption.getVoteTotalCount().getCount())
                .content(pickOption.getContents().getPickOptionContents())
                .pickDetailOptionImages(mapToPickDetailOptionImagesResponse(pickOption))
                .build();
    }

    // 익명 회원 전용
    public static PickDetailOptionResponse of(PickOption pickOption, Pick findPick, AnonymousMember anonymousMember) {
        return PickDetailOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle().getTitle())
                .isPicked(PickResponseUtils.isPickedPickOptionByAnonymousMember(findPick, pickOption, anonymousMember))
                .percent(PickOption.calculatePercentBy(findPick, pickOption))
                .voteTotalCount(pickOption.getVoteTotalCount().getCount())
                .content(pickOption.getContents().getPickOptionContents())
                .pickDetailOptionImages(mapToPickDetailOptionImagesResponse(pickOption))
                .build();
    }

    private static List<PickDetailOptionImage> mapToPickDetailOptionImagesResponse(PickOption pickOption) {
        return pickOption.getPickOptionImages().stream()
                .map(PickDetailOptionImage::from)
                .toList();
    }
}

