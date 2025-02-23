package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Subscription;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.SubscriptionRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionRepositoryCustom {
    Optional<Subscription> findByMemberIdAndCompanyId(Long memberId, Long companyId);
}
