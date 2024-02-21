package com.dreamypatisiel.devdevdev.global.config;

import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    @PersistenceContext
    private final EntityManager em;

    @Bean
    public TimeProvider timeProvider() {
        return new TimeProvider();
    }

    @Bean
    public JPQLQueryFactory jpqlQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
