package com.dreamypatisiel.devdevdev.elastic.config;

import java.time.Duration;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Profile({"local", "dev", "test", "prod"})
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.dreamypatisiel.devdevdev.elastic.domain.repository")
public class OpenSearchRestClientConfiguration extends AbstractOpenSearchConfiguration {
    @Value("${opensearch.endpoint}")
    private String endpoint;

    @Value("${opensearch.credentials.username}")
    private String username;

    @Value("${opensearch.credentials.password}")
    private String password;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public CredentialsProvider credentialsProvider() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }

    @Bean
    @Override
    public RestHighLevelClient opensearchClient() {
        CredentialsProvider credentialsProvider = applicationContext.getBean(
                CredentialsProvider.class);

        RestClientBuilder builder = RestClient.builder(new HttpHost(endpoint, 443, "https"))
                .setHttpClientConfigCallback(
                        httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(
                                credentialsProvider))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout((int) Duration.ofSeconds(600).toMillis())
                        .setSocketTimeout((int) Duration.ofSeconds(300).toMillis()));

        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}