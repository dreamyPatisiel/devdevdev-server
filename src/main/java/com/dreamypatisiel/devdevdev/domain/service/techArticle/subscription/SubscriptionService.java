package com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription;

import com.dreamypatisiel.devdevdev.web.dto.response.subscription.CompanyDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.SubscriableCompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.SubscriptionResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;

public interface SubscriptionService {
    SubscriptionResponse subscribe(Long companyId, Authentication authentication);

    void unsubscribe(Long companyId, Authentication authentication);

    Slice<SubscriableCompanyResponse> getSubscribableCompany(Pageable pageable, Long companyId,
                                                             Authentication authentication);

    CompanyDetailResponse getCompanyDetail(Long companyId, Authentication authentication);
}
