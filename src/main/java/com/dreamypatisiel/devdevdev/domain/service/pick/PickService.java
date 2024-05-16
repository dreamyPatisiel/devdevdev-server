package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickMainResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickModifyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickResponse;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.VotePickOptionRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface PickService {
    Slice<PickMainResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort,
                                          Authentication authentication);

    PickUploadImageResponse uploadImages(String name, List<MultipartFile> images);

    void deleteImage(Long pickOptionImageId);

    PickRegisterResponse registerPick(RegisterPickRequest registerPickRequest, Authentication authentication);

    PickModifyResponse modifyPick(Long pickId, ModifyPickRequest modifyPickRequest, Authentication authentication);

    PickDetailResponse findPickDetail(Long pickId, Authentication authentication);

    VotePickResponse votePickOption(VotePickOptionRequest votePickOptionRequest, Authentication authentication);
}
