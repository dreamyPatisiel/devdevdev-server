package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickService;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickServiceStrategy;
import com.dreamypatisiel.devdevdev.domain.service.response.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.controller.request.PickRegisterRequest;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "픽픽픽 API", description = "픽픽픽 메인, 상세, 작성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/devdevdev/api/v1")
public class PickController {

    private final PickServiceStrategy pickServiceStrategy;

    @Operation(summary = "픽픽픽 메인 조회", description = "픽픽픽 메인 페이지에 필요한 데이터를 조회합니다.")
    @GetMapping("/picks")
    public ResponseEntity<BasicResponse<Slice<PicksResponse>>> getPicksMain(
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long pickId,
            @RequestParam(required = false) PickSort pickSort) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        PickService pickService = pickServiceStrategy.getPickService();
        Slice<PicksResponse> response = pickService.findPicksMain(pageable, pickId, pickSort, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "픽픽픽 이미지 업로드", description = "픽픽픽 작성 단계에서 픽픽픽 옵션에 해당하는 이미지를 업로드 합니다.")
    @PostMapping(value = "/picks/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BasicResponse<PickUploadImageResponse>> uploadPickOptionImages(
            @RequestParam String name,
            @RequestPart List<MultipartFile> pickOptionImages) {

        PickService pickService = pickServiceStrategy.getPickService();
        PickUploadImageResponse pickUploadImageResponse = pickService.uploadImages(name, pickOptionImages);

        return ResponseEntity.ok(BasicResponse.success(pickUploadImageResponse));
    }

    @Operation(summary = "픽픽픽 이미지 삭제", description = "픽픽픽 이미지를 삭제합니다.")
    @DeleteMapping("/picks/image/{pickImageOptionId}")
    public ResponseEntity<BasicResponse<Void>> deletePickImage(@PathVariable Long pickImageOptionId) {

        PickService pickService = pickServiceStrategy.getPickService();
        pickService.deleteImage(pickImageOptionId);

        return ResponseEntity.ok(BasicResponse.success());
    }

    @Operation(summary = "픽픽픽 작성", description = "픽픽픽을 작성합니다.")
    @PostMapping("/picks")
    public ResponseEntity<BasicResponse<PickRegisterResponse>> registerPick(
            @RequestBody @Validated PickRegisterRequest pickRegisterRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickService pickService = pickServiceStrategy.getPickService();
        PickRegisterResponse response = pickService.registerPick(pickRegisterRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
