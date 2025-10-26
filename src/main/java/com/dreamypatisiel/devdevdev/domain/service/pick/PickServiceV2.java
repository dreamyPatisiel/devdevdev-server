package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponseV2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PickServiceV2 extends PickService {
    Slice<PickMainResponseV2> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort, String anonymousMemberId,
                                            Authentication authentication);

    List<SimilarPickResponseV2> findTop3SimilarPicksV2(Long pickId);
}