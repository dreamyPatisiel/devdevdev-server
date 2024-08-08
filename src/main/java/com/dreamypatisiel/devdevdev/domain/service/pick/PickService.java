package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.RegisterPickCommentDto;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.VotePickOptionDto;
import com.dreamypatisiel.devdevdev.domain.service.response.PickCommentResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickMainResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickModifyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.SimilarPickResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickResponse;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickRequest;
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

    PickCommentResponse registerPickComment(RegisterPickCommentDto registerPickCommentDto,
                                            Authentication authentication);
}
