package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.CompanyNameException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class CompanyName {

    public static final int MIN_COMPANY_NAME_LENGTH = 1;
    public static final int MAX_COMPANY_NAME_LENGTH = 30;
    public static final String INVALID_COMPANY_NAME_LENGTH_MESSAGE = "회사 이름의 길이는 %d부터 %d 사이여야 합니다.";

    private String companyName;

    public CompanyName(String companyName) {
        validationCompanyName(companyName);
        this.companyName = companyName;
    }

    private void validationCompanyName(String companyName) {
        if(!isValidLength(companyName)) {
            throw new CompanyNameException(getInvalidLengthExceptionMessage());
        }
    }

    private boolean isValidLength(String companyName) {
        return StringUtils.hasText(companyName) && companyName.length() <= MAX_COMPANY_NAME_LENGTH;
    }

    public static String getInvalidLengthExceptionMessage()  {
        return String.format(INVALID_COMPANY_NAME_LENGTH_MESSAGE, MIN_COMPANY_NAME_LENGTH, MAX_COMPANY_NAME_LENGTH);
    }
}
