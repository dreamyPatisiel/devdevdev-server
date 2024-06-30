package com.dreamypatisiel.devdevdev.global.utils;

import org.springframework.web.util.UriComponentsBuilder;

public class UriUtils {

    public static String createUriByDomainAndEndpoint(String domain, String endpoint) {
        return UriComponentsBuilder
                .fromUriString(domain + endpoint)
                .toUriString();
    }
}
