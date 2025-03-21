package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.dto.CompanyDetailDto;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CompanyRepositoryCustom {
    Slice<Company> findCompanyByCursor(Pageable pageable, Long companyId);

    Optional<CompanyDetailDto> findCompanyDetailDtoByMemberIdAndCompanyId(Long memberId, Long companyId);
}
