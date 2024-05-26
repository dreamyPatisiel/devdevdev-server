package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.service.response.util.PickResponseUtils;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
public class PickMainOptionResponse {
    private final Long id;
    private final String title;
    private final int percent;
    private final Boolean isPicked;

    @Builder
    public PickMainOptionResponse(Long id, Title title, BigDecimal percent, Boolean isPicked) {
        this.id = id;
        this.title = title.getTitle();
        this.percent = percent.intValueExact();
        this.isPicked = isPicked;
    }

    public static PickMainOptionResponse of(Pick pick, PickOption pickOption,
                                            Member member) {
        return PickMainOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle())
                .percent(PickOption.calculatePercentBy(pick, pickOption))
                .isPicked(PickResponseUtils.isPickedPickOptionByMember(pick, pickOption, member))
                .build();
    }

    public static PickMainOptionResponse of(Pick pick, PickOption pickOption,
                                            AnonymousMember anonymousMember) {
        return PickMainOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle())
                .percent(PickOption.calculatePercentBy(pick, pickOption))
                .isPicked(PickResponseUtils.isPickedPickOptionByAnonymousMember(pick, pickOption, anonymousMember))
                .build();
    }
}
