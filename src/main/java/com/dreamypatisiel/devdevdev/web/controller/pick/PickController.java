package com.dreamypatisiel.devdevdev.web.controller.pick;

import static com.dreamypatisiel.devdevdev.web.WebConstant.HEADER_ANONYMOUS_MEMBER_ID;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickMultiServiceHandler;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickService;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickServiceStrategy;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.VotePickOptionDto;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickModifyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.VotePickResponse;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingRequestHandler;
import com.dreamypatisiel.devdevdev.openai.data.request.EmbeddingRequest;
import com.dreamypatisiel.devdevdev.openai.data.response.Embedding;
import com.dreamypatisiel.devdevdev.openai.data.response.OpenAIResponse;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.VotePickOptionRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "픽픽픽 API", description = "픽픽픽 메인, 상세, 작성/수정/삭제, 이미지 업로드/삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/devdevdev/api/v1")
public class PickController {

    private final PickServiceStrategy pickServiceStrategy;
    private final PickMultiServiceHandler pickMultiServiceHandler;
    private final EmbeddingRequestHandler embeddingRequestHandler;

    @Operation(summary = "픽픽픽 메인 조회", description = "픽픽픽 메인 페이지에 필요한 데이터를 커서 방식으로 조회합니다.")
    @GetMapping("/picks")
    public ResponseEntity<BasicResponse<Slice<PickMainResponse>>> getPicksMain(
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long pickId,
            @RequestParam(required = false) PickSort pickSort,
            @RequestHeader(value = HEADER_ANONYMOUS_MEMBER_ID, required = false) String anonymousMemberId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickService pickService = pickServiceStrategy.getPickService();
        Slice<PickMainResponse> response = pickService.findPicksMain(pageable, pickId, pickSort, anonymousMemberId,
                authentication);

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
    @DeleteMapping("/picks/image/{pickOptionImageId}")
    public ResponseEntity<BasicResponse<Void>> deletePickImage(@PathVariable Long pickOptionImageId) {

        PickService pickService = pickServiceStrategy.getPickService();
        pickService.deleteImage(pickOptionImageId);

        return ResponseEntity.ok(BasicResponse.success());
    }

    @Operation(summary = "픽픽픽 작성", description = "픽픽픽을 작성합니다.")
    @PostMapping("/picks")
    public ResponseEntity<BasicResponse<PickRegisterResponse>> registerPick(
            @RequestBody @Validated RegisterPickRequest registerPickRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        // open ai api 호출
        OpenAIResponse<Embedding> embeddingOpenAIResponse = embeddingRequestHandler.postEmbeddings(
                EmbeddingRequest.createTextEmbedding3Small(registerPickRequest.getPickTitle()));

        PickService pickService = pickServiceStrategy.getPickService();

        pickMultiServiceHandler.injectPickService(pickService);

        // 픽픽픽 작성 및 embedding 저장
        PickRegisterResponse response = pickMultiServiceHandler.registerPickAndSaveEmbedding(
                registerPickRequest, authentication, embeddingOpenAIResponse);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "픽픽픽 수정", description = "픽픽픽을 수정합니다.")
    @PatchMapping("/picks/{pickId}")
    public ResponseEntity<BasicResponse<PickModifyResponse>> modifyPick(
            @PathVariable Long pickId,
            @RequestBody @Validated ModifyPickRequest modifyPickRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        // open ai api 호출
        OpenAIResponse<Embedding> embeddingOpenAIResponse = embeddingRequestHandler.postEmbeddings(
                EmbeddingRequest.createTextEmbedding3Small(modifyPickRequest.getPickTitle()));

        PickService pickService = pickServiceStrategy.getPickService();

        pickMultiServiceHandler.injectPickService(pickService);

        // 픽픽픽 수정 및 embedding 저장
        PickModifyResponse response = pickMultiServiceHandler.modifyPickAndSaveEmbedding(pickId,
                modifyPickRequest, authentication, embeddingOpenAIResponse);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "픽픽픽 상세 조회", description = "픽픽픽 상세 페이지를 조회합니다.")
    @GetMapping("/picks/{pickId}")
    public ResponseEntity<BasicResponse<PickDetailResponse>> getPickDetail(@PathVariable Long pickId,
                                                                           @RequestHeader(value = HEADER_ANONYMOUS_MEMBER_ID, required = false) String anonymousMemberId) {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickService pickService = pickServiceStrategy.getPickService();
        PickDetailResponse response = pickService.findPickDetail(pickId, anonymousMemberId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "픽픽픽 선택지 투표", description = "픽픽픽 상세 페이지에서 픽픽픽 선택지에 투표합니다.")
    @PostMapping("/picks/vote")
    public ResponseEntity<BasicResponse<VotePickResponse>> votePickOption(
            @RequestBody @Validated VotePickOptionRequest votePickOptionRequest,
            @RequestHeader(value = HEADER_ANONYMOUS_MEMBER_ID, required = false) String anonymousMemberId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickService pickService = pickServiceStrategy.getPickService();

        VotePickOptionDto votePickOptionDto = VotePickOptionDto.of(votePickOptionRequest, anonymousMemberId);
        VotePickResponse response = pickService.votePickOption(votePickOptionDto, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "픽픽픽 삭제", description = "픽픽픽과 관련 있는 모든 데이터를 삭제합니다.")
    @DeleteMapping("/picks/{pickId}")
    public ResponseEntity<BasicResponse<Void>> deletePick(@PathVariable Long pickId) {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickService pickService = pickServiceStrategy.getPickService();
        pickService.deletePick(pickId, authentication);

        return ResponseEntity.ok(BasicResponse.success());
    }

    @Operation(summary = "나도 고민했는데 픽픽픽", description = "픽픽픽 상세와 유사한 성격의 픽픽픽 3개를 추천합니다.")
    @GetMapping("picks/{pickId}/similarties")
    public ResponseEntity<BasicResponse<SimilarPickResponse>> getSimilarPicks(@PathVariable Long pickId) {

        PickService pickService = pickServiceStrategy.getPickService();
        List<SimilarPickResponse> response = pickService.findTop3SimilarPicks(pickId);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
