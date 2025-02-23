package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

public interface SubscriptionRepositoryCustom {
    Boolean existsByMemberIdAndCompanyId(Long memberId, Long companyId);
}
