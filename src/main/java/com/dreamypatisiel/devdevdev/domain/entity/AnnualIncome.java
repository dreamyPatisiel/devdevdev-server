package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.exception.AnnualIncomeException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class AnnualIncome {
    public static final String INVALID_ANNUAL_INCOME_MESSAGE = "연봉은 1원에서 1,000,000 만원 사이의 값이어야 합니다.";
    private static final int MIN_ANNUAL_INCOME = 0;
    private static final int MAX_ANNUAL_INCOME = 10_000_000;
    private Integer annualIncome;

    public AnnualIncome(Integer annualIncome) {
        validationAnnualIncome(annualIncome);
        this.annualIncome = annualIncome;
    }

    private void validationAnnualIncome(Integer annualIncome) {
        if(!isAnnualIncomeRange(annualIncome)) {
            throw new AnnualIncomeException(INVALID_ANNUAL_INCOME_MESSAGE);
        }
    }

    private boolean isAnnualIncomeRange(Integer annualIncome) {
        return annualIncome > MIN_ANNUAL_INCOME && annualIncome <= MAX_ANNUAL_INCOME;
    }


}
