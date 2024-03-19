package com.dreamypatisiel.devdevdev.elastic.config;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class ContainerExtension implements BeforeAllCallback {

    static GenericContainer<?> esContainer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        System.out.println("123123123");
        esContainer = new GenericContainer<>(
                new ImageFromDockerfile()
                        .withDockerfileFromBuilder(builder -> builder
                                .from("docker.elastic.co/elasticsearch/elasticsearch:7.17.10")
                                .run("bin/elasticsearch-plugin install analysis-nori")
                                .build()))
                .withEnv("discovery.type", "single-node")
                .withEnv("http.host", "0.0.0.0")
                .withFileSystemBind("./dict", "/usr/share/elasticsearch/config/dict", BindMode.READ_ONLY)
                .withExposedPorts(9200)
                .withReuse(true);

        esContainer.start();
        System.setProperty("spring.elasticsearch.rest.uris", esContainer.getHost());
        String host = String.format("http://%s:%s", esContainer.getContainerIpAddress(), esContainer.getMappedPort(9200));
    }
}