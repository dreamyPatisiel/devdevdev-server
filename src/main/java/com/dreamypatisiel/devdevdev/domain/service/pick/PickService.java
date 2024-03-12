package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface PickService {
    Slice<PicksResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort, Authentication authentication);
    Long registerPick(RegisterPickRequest registerPickRequest, Map<String, List<MultipartFile>> registerPickImageFiles, Authentication authentication);
    Long registerPick(RegisterPickRequest registerPickRequest, Authentication authentication);
}
