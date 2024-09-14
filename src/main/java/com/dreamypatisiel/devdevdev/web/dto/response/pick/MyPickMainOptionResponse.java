package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.web.dto.util.PickResponseUtils;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
public class MyPickMainOptionResponse {
    private final Long id;
    private final String title;
    private final int percent;
    private final Boolean isPicked;

    @Builder
    public MyPickMainOptionResponse(Long id, Title title, BigDecimal percent, Boolean isPicked) {
        this.id = id;
        this.title = title.getTitle();
        this.percent = percent.intValueExact();
        this.isPicked = isPicked;
    }

    public static MyPickMainOptionResponse of(Pick pick, PickOption pickOption) {
        return MyPickMainOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle())
                .percent(PickOption.calculatePercentBy(pick, pickOption))
                .isPicked(PickResponseUtils.isPickedPickOptionByMember(pick, pickOption, pick.getMember()))
                .build();
    }
}
