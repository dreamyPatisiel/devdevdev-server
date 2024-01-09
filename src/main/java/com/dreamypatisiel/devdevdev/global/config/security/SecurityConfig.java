package com.dreamypatisiel.devdevdev.global.config.security;

import static com.dreamypatisiel.devdevdev.global.config.security.SecurityConstant.PREFLIGHT_MAX_AGE;
import static com.dreamypatisiel.devdevdev.global.config.security.SecurityConstant.SPRING_H2_CONSOLE_ENABLED;
import static com.dreamypatisiel.devdevdev.global.config.security.SecurityConstant.WILDCARD_PATTERN;

import com.dreamypatisiel.devdevdev.global.config.security.oauth2.service.AppOAuth2MemberService;
import com.dreamypatisiel.devdevdev.global.config.security.oauth2.service.AppOAuth2UserService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppOAuth2UserService appOAuth2UserService;

    @Bean
    @Profile({"test", "local"})
    public SecurityFilterChain securityFilterChainOnLocal(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(apiCorsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(WILDCARD_PATTERN)
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        http.oauth2Login(oauth2 -> oauth2.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                .userService(appOAuth2UserService)));

        return http.build();
    }

    @Bean
    protected CorsConfigurationSource apiCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(CorsConfiguration.ALL));
        configuration.setAllowedMethods(List.of(CorsConfiguration.ALL));
        configuration.setAllowedHeaders(List.of(CorsConfiguration.ALL));
        configuration.setMaxAge(PREFLIGHT_MAX_AGE);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(WILDCARD_PATTERN, configuration);

        return source;
    }

    @Bean
    @Profile("local")
    @ConditionalOnProperty(name = SPRING_H2_CONSOLE_ENABLED, havingValue = "true")
    public WebSecurityCustomizer configH2ConsoleEnable() {
        return web -> web.ignoring()
                .requestMatchers(PathRequest.toH2Console());
    }
}
