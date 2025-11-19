package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.global.utils.MarkdownUtils;
import com.dreamypatisiel.devdevdev.web.dto.util.PickResponseUtils;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;

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
        if (pickOption == null || pickOption.getContents() == null) {
            return null;
        }

        String content = pickOption.getContents().getPickOptionContents();
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }

        // 마크다운 문법 제거 및 300자 제한
        String text = MarkdownUtils.convertMarkdownToText(content);

        // 300자 제한
        if (text.length() > 300) {
            return text.substring(0, 300);
        }
        return text;
    }
}

