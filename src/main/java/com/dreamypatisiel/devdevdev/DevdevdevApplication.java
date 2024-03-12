package com.dreamypatisiel.devdevdev;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@OpenAPIDefinition
@ConfigurationPropertiesScan
@EnableJpaAuditing
@SpringBootApplication
public class DevdevdevApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevdevdevApplication.class, args);
	}

}
