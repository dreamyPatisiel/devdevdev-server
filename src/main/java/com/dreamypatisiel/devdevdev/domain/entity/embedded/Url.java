package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.UrlException;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Url {

    private static final String URL_REGEX =
            "^((http|https)://)?([a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+)(/[a-zA-Z0-9-]*)*(\\?\\S*)?$";
    public static final String INVALID_URL_MESSAGE = "알맞은 URL 형식이 아닙니다.";

    private String url;

    public Url(String url) {
        urlValidation(url);
        this.url = url;
    }

    private void urlValidation(String url) {
        UrlValidator instance = UrlValidator.getInstance();
        boolean valid = instance.isValid(url);
        if (!valid) {
            throw new UrlException(INVALID_URL_MESSAGE);
        }
//        if (!isValidUrl(url)) {
//            throw new UrlException(INVALID_URL_MESSAGE);
//        }
    }

    private boolean isValidUrl(String url) {
        return Pattern.matches(URL_REGEX, url);
    }
}
