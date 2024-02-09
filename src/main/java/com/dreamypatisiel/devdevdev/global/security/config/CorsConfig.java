package com.dreamypatisiel.devdevdev.global.security.config;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.PREFLIGHT_MAX_AGE;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.WILDCARD_PATTERN;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    public static final String LOCAL_ORIGIN = "http://localhost:3000";
    public static final String DEV_ORIGIN = "https://dev.devdevdev.co.kr";
    public static final String PROD_ORIGIN = "https://www.devdevdev.co.kr";

    @Bean
    protected CorsConfigurationSource apiCorsConfigurationSource() {
        List<String> origins = List.of(LOCAL_ORIGIN, DEV_ORIGIN, PROD_ORIGIN);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(List.of(CorsConfiguration.ALL));
        configuration.setAllowedHeaders(List.of(CorsConfiguration.ALL));
        configuration.setMaxAge(PREFLIGHT_MAX_AGE);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(WILDCARD_PATTERN, configuration);

        return source;
    }
}
