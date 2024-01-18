package com.dreamypatisiel.devdevdev.global.config;

import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public TimeProvider timeProvider() {
        return new TimeProvider();
    }
}
