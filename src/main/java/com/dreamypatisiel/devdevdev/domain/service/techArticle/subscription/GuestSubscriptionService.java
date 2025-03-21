package com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import static com.dreamypatisiel.devdevdev.domain.exception.GuestExceptionMessage.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.SubscriableCompanyResponse;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
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
}
