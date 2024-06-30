package com.dreamypatisiel.devdevdev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.dreamypatisiel.devdevdev.domain.repository"})
@ConfigurationPropertiesScan
@EnableJpaAuditing
@SpringBootApplication
public class DevdevdevApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevdevdevApplication.class, args);
    }

}
