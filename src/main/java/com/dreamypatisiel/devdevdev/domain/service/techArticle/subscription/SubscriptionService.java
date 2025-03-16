package com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Subscription;
import com.dreamypatisiel.devdevdev.domain.exception.CompanyExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.exception.SubscriptionExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.SubscriptionException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.SubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final MemberProvider memberProvider;
    private final CompanyRepository companyRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * @Note: 기업 구독하기
     * @Author: 장세웅
     * @Since: 2025-02-23
     */
    @Transactional
    public SubscriptionResponse subscribe(Long companyId, Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기업 기술블로그 구독 이력 조회
        Boolean isSubscription = subscriptionRepository.existsByMemberIdAndCompanyId(findMember.getId(), companyId);

        // 이미 구독한 경우
        if (isSubscription) {
            throw new SubscriptionException(SubscriptionExceptionMessage.ALREADY_SUBSCRIBED_COMPANY_MESSAGE);
        }

        // 기업 조회
        Company findCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException(CompanyExceptionMessage.NOT_FOUND_COMPANY_MESSAGE));

        // 생성 및 저장
        Subscription subscription = subscriptionRepository.save(Subscription.create(findMember, findCompany));

        return new SubscriptionResponse(subscription.getId());
    }

    /**
     * @Note: 기업 구독 취소
     * @Author: 장세웅
     * @Since: 2025-02-24
     */
    @Transactional
    public void unsubscribe(Long companyId, Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 기업 기술블로그 구독 이력 조회
        // Todo: 소영님 아래 쿼리에서 member, company left join 발생하는데 이유를 알까요?
        Subscription findSubscription = subscriptionRepository.findByMemberAndCompanyId(findMember, companyId)
                .orElseThrow(() -> new NotFoundException(SubscriptionExceptionMessage.NOT_FOUND_SUBSCRIPTION_MESSAGE));

        // 삭제
        subscriptionRepository.delete(findSubscription);
    }
}
