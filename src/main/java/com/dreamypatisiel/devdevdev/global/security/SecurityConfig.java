package com.dreamypatisiel.devdevdev.global.security;

import static com.dreamypatisiel.devdevdev.global.security.SecurityConstant.PREFLIGHT_MAX_AGE;
import static com.dreamypatisiel.devdevdev.global.security.SecurityConstant.SPRING_H2_CONSOLE_ENABLED;
import static com.dreamypatisiel.devdevdev.global.security.SecurityConstant.WILDCARD_PATTERN;

import com.dreamypatisiel.devdevdev.global.security.filter.SecurityExceptionFilter;
import com.dreamypatisiel.devdevdev.global.security.jwt.JwtFilter;
import com.dreamypatisiel.devdevdev.global.security.jwt.TokenService;
import com.dreamypatisiel.devdevdev.global.security.jwt.handler.JwtAccessDeniedHandler;
import com.dreamypatisiel.devdevdev.global.security.jwt.handler.JwtAuthenticationEntryPointHandler;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.handler.OAuth2AuthenticationFailureHandler;
import com.dreamypatisiel.devdevdev.global.security.oauth2.handler.OAuth2SuccessHandler;
import com.dreamypatisiel.devdevdev.global.security.oauth2.service.OAuth2UserServiceImpl;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserServiceImpl oAuth2UserServiceImpl;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPointHandler jwtAuthenticationEntryPointHandler;
    private final JwtFilter jwtFilter;
    private final SecurityExceptionFilter securityExceptionFilter;

    @Bean
    @Profile({"test", "local"})
    public SecurityFilterChain securityFilterChainOnLocal(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(apiCorsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(SecurityConstant.WHITELIST_URL).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPointHandler)
                .accessDeniedHandler(jwtAccessDeniedHandler));

        http.oauth2Login(oauth2 -> oauth2.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                .userService(oAuth2UserServiceImpl))
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
                .authorizationEndpoint(authorizationEndpointConfig -> authorizationEndpointConfig
                        .baseUri(SecurityConstant.OAUTH2_LOGIN_URL_PREFIX)
                )
                .redirectionEndpoint(redirectionEndpointConfig -> redirectionEndpointConfig
                        .baseUri(SecurityConstant.OAUTH2_REDIRECT_URL_PREFIX))
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(securityExceptionFilter, JwtFilter.class);

        http.logout(customizer -> customizer
                .logoutUrl("/devdevdev/api/v1/logout")
                .logoutSuccessUrl("/")
                .deleteCookies(JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN, JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN)
                .permitAll());

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
