package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickService;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickServiceStrategy;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
//        Slice<PicksResponse> response = pickService.findPicksMain(pageable, pickId, pickSort, authentication);
        PickService pickService = pickServiceStrategy.getPickService();
        Slice<PicksResponse> response = pickService.findPicksMain(pageable, pickId, pickSort, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
