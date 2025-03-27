package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.dto.CompanyDetailDto;
import java.util.Optional;

import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CompanyRepositoryCustom {
    Slice<Company> findCompanyByCursor(Pageable pageable, Long companyId);

    Optional<CompanyDetailDto> findCompanyDetailDtoByMemberIdAndCompanyId(Long memberId, Long companyId);

    SliceCustom<Company> findSubscribedCompaniesByMemberByCursor(Pageable pageable, Long companyId, Long memberId);
}
