package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QCompany.company;
import static com.dreamypatisiel.devdevdev.domain.entity.QSubscription.subscription;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.dto.CompanyDetailDto;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.dto.QCompanyDetailDto;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepositoryCustom {

    private final JPQLQueryFactory query;

    @Override
    public Slice<Company> findCompanyByCursor(Pageable pageable, Long companyId) {
        List<Company> contents = query.selectFrom(company)
                .where(getCursorCondition(companyId))
                .orderBy(company.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        return new SliceImpl<>(contents, pageable, contents.size() >= pageable.getPageSize());
    }

    @Override
    public Optional<CompanyDetailDto> findCompanyDetailDtoByMemberIdAndCompanyId(Long memberId, Long companyId) {

        CompanyDetailDto companyDetailDto = query.select(
                        new QCompanyDetailDto(
                                company.id,
                                company.careerUrl.url,
                                company.name.companyName,
                                company.industry,
                                company.officialImageUrl.url,
                                company.description,
                                subscription.id
                        ))
                .from(company)
                .leftJoin(subscription).on(subscription.company.id.eq(company.id)
                        .and(subscription.member.id.eq(memberId)))
                .where(company.id.eq(companyId))
                .fetchOne();

        return Optional.ofNullable(companyDetailDto);
    }

    @Override
    public SliceCustom<Company> findSubscribedCompaniesByMemberByCursor(Pageable pageable, Long companyId, Long memberId) {
        // cursor 기준으로 회사 조회하되, subscription 테이블과 left join하여 member의 구독 여부가 있는지 확인해서 구독한 회사만
        List<Company> contents = query.selectFrom(company)
                .leftJoin(subscription).on(subscription.company.id.eq(company.id)
                        .and(subscription.member.id.eq(memberId)))
                .where(getCursorCondition(companyId),
                        (subscription.id.isNotNull()))
                .orderBy(company.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        // 다음 페이지 존재 여부
        boolean hasNext = contents.size() >= pageable.getPageSize();

        // 구독한 회사 총 갯수
        Long subscribedCompanyTotalCount = countSubscribedCompany(memberId);

        return new SliceCustom<>(contents, pageable, hasNext, subscribedCompanyTotalCount);
    }

    // 구독한 회사 총 개수
    private Long countSubscribedCompany(Long memberId) {
        return query.selectFrom(subscription)
                .where(subscription.member.id.eq(memberId))
                .fetchCount();
    }

    private BooleanExpression getCursorCondition(Long companyId) {
        if (companyId == null) {
            return null;
        }

        return company.id.lt(companyId);
    }
}
