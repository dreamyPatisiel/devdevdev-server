package com.dreamypatisiel.devdevdev.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

@Profile({"local", "dev"})
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){
        SecurityScheme accessToken = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER).name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("accessToken");

        return new OpenAPI()
                .info(apiInfo())
                .components(new Components().addSecuritySchemes("accessToken", accessToken))
                .security(Collections.singletonList(securityRequirement));
    }

    private Info apiInfo() {
        return new Info()
                .title("Devdevdev API 명세서")
                .description("Devdevdev API 개발 명세서")
                .version("v1");
    }

    @Bean
    public GroupedOpenApi getAllApi() {
        return GroupedOpenApi
                .builder()
                .group("all")
                .pathsToMatch("/devdevdev/api/v1/**")
                .build();
    }
}
