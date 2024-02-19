package com.dreamypatisiel.devdevdev.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "cors")
@TestPropertySource("classpath:application-test.yml")
public class CorsProperties {
    private List<String> origin = new ArrayList<>();

    public List<String> getOrigin() {
        return origin;
    }

    public void setOrigin(List<String> origin) {
        this.origin = origin;
    }

}