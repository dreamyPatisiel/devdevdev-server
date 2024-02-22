package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.PickService;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/devdevdev/api/v1")
public class PickController {

    private final PickService pickService;

    @GetMapping("picks")
    public ResponseEntity<BasicResponse<Slice<PicksResponse>>> getPicksMain(
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable, Long pickId, PickSort pickSort) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        Slice<PicksResponse> response = pickService.findPicksMain(pageable, pickId, pickSort, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
