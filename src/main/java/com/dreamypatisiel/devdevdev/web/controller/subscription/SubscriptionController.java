package com.dreamypatisiel.devdevdev.web.controller.subscription;

import com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleServiceStrategy;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.SubscriptionService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.request.subscription.SubscribeCompanyRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.CompanyDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.SubscriableCompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.SubscriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "구독 API", description = "구독 관련 기능")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class SubscriptionController {

    private final TechArticleServiceStrategy techArticleServiceStrategy;

    @Operation(summary = "기업 구독하기", description = "구독 가능한 기업을 구독합니다.")
    @PostMapping("/subscriptions")
    public ResponseEntity<BasicResponse<SubscriptionResponse>> subscribe(
            @RequestBody @Validated SubscribeCompanyRequest request) {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        SubscriptionService subscriptionService = techArticleServiceStrategy.getSubscriptionService();

        SubscriptionResponse response = subscriptionService.subscribe(request.getCompanyId(), authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "기업 구독 취소", description = "구독한 기업을 구독 취소 합니다.")
    @DeleteMapping("/subscriptions")
    public ResponseEntity<BasicResponse<Void>> unsubscribe(@RequestBody @Validated SubscribeCompanyRequest request) {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        SubscriptionService subscriptionService = techArticleServiceStrategy.getSubscriptionService();

        subscriptionService.unsubscribe(request.getCompanyId(), authentication);

        return ResponseEntity.ok(BasicResponse.success());
    }

    @Operation(summary = "구독한 가능한 기업 목록 조회", description = "구독 가능한 기업 목록을 조회합니다.")
    @GetMapping("/subscriptions/companies")
    public ResponseEntity<BasicResponse<Slice<SubscriableCompanyResponse>>> getSubscriptions(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long companyId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        SubscriptionService subscriptionService = techArticleServiceStrategy.getSubscriptionService();

        Slice<SubscriableCompanyResponse> response = subscriptionService.getSubscribableCompany(pageable,
                companyId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "구독 가능한 기업 상세 정보 조회", description = "구독 가능한 기업의 상세 정보를 조회합니다.")
    @GetMapping("/subscriptions/companies/{companyId}")
    public ResponseEntity<BasicResponse<CompanyDetailResponse>> getCompanyDetail(@PathVariable Long companyId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        SubscriptionService subscriptionService = techArticleServiceStrategy.getSubscriptionService();

        CompanyDetailResponse response = subscriptionService.getCompanyDetail(companyId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
