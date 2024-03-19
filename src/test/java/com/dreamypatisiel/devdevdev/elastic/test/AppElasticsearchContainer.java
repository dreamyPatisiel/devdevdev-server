package com.dreamypatisiel.devdevdev.elastic.test;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class AppElasticsearchContainer extends ElasticsearchContainer {

    public static GenericContainer<?> container = new GenericContainer<>(
                    new ImageFromDockerfile()
                    .withDockerfileFromBuilder(builder -> builder
            .from("docker.elastic.co/elasticsearch/elasticsearch:7.17.10")
            .run("bin/elasticsearch-plugin install analysis-nori")
                            .build()))
            .withEnv("discovery.type", "single-node")
            .withEnv("http.host", "0.0.0.0")
            .withFileSystemBind("./dict", "/usr/share/elasticsearch/config/dict",BindMode.READ_ONLY)
            .withExposedPorts(9200)
            .withReuse(true);
}
