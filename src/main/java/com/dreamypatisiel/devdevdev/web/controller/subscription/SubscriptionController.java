package com.dreamypatisiel.devdevdev.web.controller.subscription;

import com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleServiceStrategy;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.SubscriptionService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.SubscriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "구독 API", description = "구독 관련 기능")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class SubscriptionController {

    private final TechArticleServiceStrategy techArticleServiceStrategy;

    /**
     * @Note: 기업 구독하기
     * @Author: 장세웅
     * @Since: 2025-02-24
     */
    @Operation(summary = "기업 구독하기", description = "구독 가능한 기업을 구독합니다.")
    @PostMapping("/subscription/{companyId}")
    public ResponseEntity<BasicResponse<SubscriptionResponse>> subscribe(@PathVariable Long companyId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        SubscriptionService subscriptionService = techArticleServiceStrategy.getSubscriptionService();

        SubscriptionResponse response = subscriptionService.subscribe(companyId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    /**
     * @Note: 기업 구독 취소
     * @Author: 장세웅
     * @Since: 2025-02-24
     */
    @Operation(summary = "기업 구독 취소", description = "구독한 기업을 구독 취소 합니다.")
    @DeleteMapping("/subscription/{companyId}")
    public ResponseEntity<BasicResponse<Void>> unsubscribe(@PathVariable Long companyId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        SubscriptionService subscriptionService = techArticleServiceStrategy.getSubscriptionService();

        subscriptionService.unsubscribe(companyId, authentication);

        return ResponseEntity.ok(BasicResponse.success());
    }
}
