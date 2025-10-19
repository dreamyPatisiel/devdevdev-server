package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PickServiceV2 extends PickService {
    Slice<PickMainResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort, String anonymousMemberId,
                                          Authentication authentication);

    PickDetailResponse findPickDetail(Long pickId, String anonymousMemberId, Authentication authentication);

    List<SimilarPickResponse> findTop3SimilarPicks(Long pickId);
}