package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.web.dto.util.PickResponseUtils;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PickMainOptionResponseV2 {
    private final Long id;
    private final String title;
    private final int percent;
    private final Boolean isPicked;
    private final String content;
    private final String thumbnailImageUrl;

    @Builder
    public PickMainOptionResponseV2(Long id, Title title, BigDecimal percent, Boolean isPicked,
                                    String content, String thumbnailImageUrl) {
        this.id = id;
        this.title = title.getTitle();
        this.percent = percent.intValueExact();
        this.isPicked = isPicked;
        this.content = content;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    // 회원 전용
    public static PickMainOptionResponseV2 of(Pick pick, PickOption pickOption, Member member) {
        return PickMainOptionResponseV2.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle())
                .percent(PickOption.calculatePercentBy(pick, pickOption))
                .isPicked(PickResponseUtils.isPickedPickOptionByMember(pick, pickOption, member))
                .content(getContentWithLengthLimit(pickOption))
                .thumbnailImageUrl(getThumbnailImageUrl(pickOption))
                .build();
    }

    // 익명 회원 전용
    public static PickMainOptionResponseV2 of(Pick pick, PickOption pickOption, AnonymousMember anonymousMember) {
        return PickMainOptionResponseV2.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle())
                .percent(PickOption.calculatePercentBy(pick, pickOption))
                .isPicked(PickResponseUtils.isPickedPickOptionByAnonymousMember(pick, pickOption, anonymousMember))
                .content(getContentWithLengthLimit(pickOption))
                .thumbnailImageUrl(getThumbnailImageUrl(pickOption))
                .build();
    }

    public static String getThumbnailImageUrl(PickOption pickOption) {
        List<String> imageUrls = pickOption.getPickOptionImages().stream()
                .map(PickOptionImage::getImageUrl)
                .toList();

        return imageUrls.isEmpty() ? null : imageUrls.getFirst();
    }

    public static String getContentWithLengthLimit(PickOption pickOption) {
        String content = pickOption.getContents().getPickOptionContents();
        if (content == null) {
            return null;
        }

        // 300자 제한
        if (content.length() > 300) {
            return content.substring(0, 300);
        }
        return content;
    }
}

