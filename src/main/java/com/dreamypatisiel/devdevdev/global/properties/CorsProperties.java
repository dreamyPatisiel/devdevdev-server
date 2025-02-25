package com.dreamypatisiel.devdevdev.global.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cors")
@RequiredArgsConstructor
public class CorsProperties {
    private List<String> origin = new ArrayList<>();

    public List<String> getOrigin() {
        return this.origin;
    }

    public List<String> getUnmodifiableOrigins() {
        return Collections.unmodifiableList(origin);
    }

    public void setOrigin(List<String> origin) {
        this.origin = origin;
    }
}