package com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription;

import static com.dreamypatisiel.devdevdev.domain.exception.GuestExceptionMessage.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.exception.CompanyExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.CompanyDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.SubscriableCompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.SubscriptionResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestSubscriptionService implements SubscriptionService {

    private final CompanyRepository companyRepository;
    private final TechArticleRepository techArticleRepository;

    @Override
    public SubscriptionResponse subscribe(Long companyId, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public void unsubscribe(Long companyId, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public Slice<SubscriableCompanyResponse> getSubscribableCompany(Pageable pageable, Long companyId,
                                                                    Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 기업 목록 조회
        Slice<Company> findCompanies = companyRepository.findCompanyByCursor(pageable, companyId);

        // 데이터 가공
        List<SubscriableCompanyResponse> response = findCompanies.getContent().stream()
                .map(SubscriableCompanyResponse::create)
                .collect(Collectors.toList());

        // 응답 생성
        return new SliceImpl<>(response, pageable, findCompanies.hasNext());
    }

    @Override
    public CompanyDetailResponse getCompanyDetail(Long companyId, Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 기업 조회
        Company findCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException(CompanyExceptionMessage.NOT_FOUND_COMPANY_MESSAGE));

        // 회사의 기술 블로그 총 갯수 조회
        Long techArticleTotalCount = techArticleRepository.countByCompanyId(companyId);

        // 응답 생성
        return CompanyDetailResponse.createGuestCompanyDetailResponse(findCompany, techArticleTotalCount);
    }
}
