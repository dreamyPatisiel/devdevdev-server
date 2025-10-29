package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Slice;

@Data
@Builder
public class PickMainSliceResponseV2 {
    private final Slice<PickMainResponseV2> picks;
    private final long totalElements;

    public static PickMainSliceResponseV2 of(Slice<PickMainResponseV2> picks, long totalElements) {
        return PickMainSliceResponseV2.builder()
                .picks(picks)
                .totalElements(totalElements)
                .build();
    }
}