package com.dreamypatisiel.devdevdev.global.security.config;

import com.dreamypatisiel.devdevdev.global.properties.CorsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.PREFLIGHT_MAX_AGE;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.WILDCARD_PATTERN;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    public List<String> origins;

    @Autowired
    CorsProperties corsProperties;

    @Bean
    protected CorsConfigurationSource apiCorsConfigurationSource() {

        origins = corsProperties.getUnmodifiableOrigins();

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
