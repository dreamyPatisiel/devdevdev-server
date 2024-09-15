package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.VotePickOptionDto;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickModifyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.VotePickResponse;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface PickService {
    Slice<PickMainResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort, String anonymousMemberId,
                                          Authentication authentication);

    PickUploadImageResponse uploadImages(String name, List<MultipartFile> images);

    void deleteImage(Long pickOptionImageId);

    PickRegisterResponse registerPick(RegisterPickRequest registerPickRequest, Authentication authentication);

    PickModifyResponse modifyPick(Long pickId, ModifyPickRequest modifyPickRequest, Authentication authentication);

    PickDetailResponse findPickDetail(Long pickId, String anonymousMemberId, Authentication authentication);

    VotePickResponse votePickOption(VotePickOptionDto votePickOptionDto, Authentication authentication);

    void deletePick(Long pickId, Authentication authentication);

    List<SimilarPickResponse> findTop3SimilarPicks(Long pickId);
}
