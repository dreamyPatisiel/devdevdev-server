package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Subscription;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.SubscriptionRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionRepositoryCustom {
    Optional<Subscription> findByMemberAndCompanyId(Member member, Long companyId);

    List<Subscription> findByMemberAndCompanyIn(Member member, List<Company> companies);

    @EntityGraph(attributePaths = {"member"})
    List<Subscription> findWithMemberByCompanyIdOrderByMemberDesc(Long companyId);
}