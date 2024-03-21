package com.dreamypatisiel.devdevdev;

import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = ElasticTechArticleRepository.class))
@ConfigurationPropertiesScan
@EnableJpaAuditing
@SpringBootApplication
public class DevdevdevApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevdevdevApplication.class, args);
	}

}
