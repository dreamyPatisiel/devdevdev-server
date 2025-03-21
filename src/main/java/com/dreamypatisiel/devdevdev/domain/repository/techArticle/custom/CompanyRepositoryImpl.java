package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import static com.dreamypatisiel.devdevdev.domain.entity.QCompany.company;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
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

    private BooleanExpression getCursorCondition(Long companyId) {
        if (companyId == null) {
            return null;
        }

        return company.id.lt(companyId);
    }
}
