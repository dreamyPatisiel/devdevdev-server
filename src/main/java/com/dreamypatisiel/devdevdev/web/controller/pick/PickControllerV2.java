package com.dreamypatisiel.devdevdev.web.controller.pick;

import static com.dreamypatisiel.devdevdev.web.WebConstant.HEADER_ANONYMOUS_MEMBER_ID;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickServiceStrategy;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickServiceV2;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.global.utils.HttpRequestUtils;
import com.dreamypatisiel.devdevdev.web.controller.ApiVersion;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickDetailResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainSearchResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponseV2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "픽픽픽 API V2", description = "픽픽픽 메인, 픽픽픽 검색, 나도 고민했는데 픽픽픽")
@RestController
@RequiredArgsConstructor
@RequestMapping("/devdevdev/api/v2")
public class PickControllerV2 {

    private final PickServiceStrategy pickServiceStrategy;

    @Operation(summary = "픽픽픽 메인 조회 V2", description = "픽픽픽 메인 페이지에 필요한 데이터를 커서 방식으로 조회합니다.")
    @GetMapping("/picks")
    public ResponseEntity<BasicResponse<Slice<PickMainResponseV2>>> getPicksMain(
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long pickId,
            @RequestParam(required = false) PickSort pickSort) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        String anonymousMemberId = HttpRequestUtils.getHeaderValue(HEADER_ANONYMOUS_MEMBER_ID);

        PickServiceV2 pickService = (PickServiceV2) pickServiceStrategy.getPickService(ApiVersion.V2);
        Slice<PickMainResponseV2> response = pickService.findPicksMain(pageable, pickId, pickSort, anonymousMemberId,
                authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "픽픽픽 상세 조회 V2", description = "픽픽픽 상세 페이지를 조회합니다.")
    @GetMapping("/picks/{pickId}")
    public ResponseEntity<BasicResponse<PickDetailResponseV2>> getPickDetail(@PathVariable Long pickId,
                                                                           @RequestHeader(value = HEADER_ANONYMOUS_MEMBER_ID, required = false) String anonymousMemberId) {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickServiceV2 pickService = (PickServiceV2) pickServiceStrategy.getPickService(ApiVersion.V2);
        PickDetailResponseV2 response = pickService.findPickDetail(pickId, anonymousMemberId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "나도 고민했는데 픽픽픽 V2", description = "픽픽픽 상세와 유사한 성격의 픽픽픽 3개를 추천합니다.")
    @GetMapping("picks/{pickId}/similarties")
    public ResponseEntity<BasicResponse<SimilarPickResponseV2>> getSimilarPicks(@PathVariable Long pickId) {

        PickServiceV2 pickService = (PickServiceV2) pickServiceStrategy.getPickService(ApiVersion.V2);
        List<SimilarPickResponseV2> response = pickService.findTop3SimilarPicksV2(pickId);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "픽픽픽 검색 V2", description = "픽픽픽 메인에서 검색한 결과를 커서방식으로 조회합니다.")
    @GetMapping("/picks/search")
    public ResponseEntity<BasicResponse<Slice<PickMainSearchResponseV2>>> searchPicksMain(
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long pickId,
            @RequestParam(required = false) Double searchScore,
            @RequestParam String keyword,
            @RequestHeader(value = HEADER_ANONYMOUS_MEMBER_ID, required = false) String anonymousMemberId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        PickServiceV2 pickService = (PickServiceV2) pickServiceStrategy.getPickService(ApiVersion.V2);
        Slice<PickMainSearchResponseV2> response = pickService.findPickMainSearch(pageable, pickId, searchScore,
                keyword, anonymousMemberId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
