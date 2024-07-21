//package com.dreamypatisiel.devdevdev.elastic.config;
//
//import org.elasticsearch.client.RestHighLevelClient;
//import org.junit.jupiter.api.Disabled;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Profile;
//import org.springframework.data.elasticsearch.client.ClientConfiguration;
//import org.springframework.data.elasticsearch.client.RestClients;
//import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
//import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
//
//@Disabled
//@Profile("test")
//@TestConfiguration
//@EnableElasticsearchRepositories(basePackages = {"com.dreamypatisiel.devdevdev.elastic.domain.repository"})
//public class ElasticsearchTestConfig extends AbstractElasticsearchConfiguration {
//
//    @Value("${elasticsearch.host}")
//    private String host;
//    @Value("${elasticsearch.port}")
//    private String port;
//
//    @Override
//    public RestHighLevelClient elasticsearchClient() {
//        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
//                .connectedTo(host + ":" + port)
//                .build();
//        return RestClients.create(clientConfiguration).rest();
//    }
//
//    @Bean
//    public ElasticsearchOperations elasticsearchOperations(RestHighLevelClient client) {
//        return new ElasticsearchRestTemplate(client);
//    }
//}