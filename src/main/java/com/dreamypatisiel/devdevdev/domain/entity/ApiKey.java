package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiKey extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private String apiKey;

    @Builder
    private ApiKey(String serviceName, String apiKey) {
        this.serviceName = serviceName;
        this.apiKey = apiKey;
    }

    public boolean isEqualsKey(String apiKey) {
        return this.apiKey.equals(apiKey);
    }
}
