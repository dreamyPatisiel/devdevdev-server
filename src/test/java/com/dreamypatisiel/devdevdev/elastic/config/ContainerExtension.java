//package com.dreamypatisiel.devdevdev.elastic.config;
//
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.extension.AfterAllCallback;
//import org.junit.jupiter.api.extension.BeforeAllCallback;
//import org.junit.jupiter.api.extension.ExtensionContext;
//import org.testcontainers.containers.BindMode;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.images.builder.ImageFromDockerfile;
//
//@Disabled
//public class ContainerExtension implements BeforeAllCallback, AfterAllCallback {
//
//    static GenericContainer<?> esContainer;
//    private static boolean initialized = false;
//
//    @Override
//    public void beforeAll(ExtensionContext extensionContext) throws Exception {
//        if (!initialized) {
//            esContainer = new GenericContainer<>(
//                    new ImageFromDockerfile()
//                            .withDockerfileFromBuilder(builder -> builder
//                                    .from("docker.elastic.co/elasticsearch/elasticsearch:7.17.10")
//                                    .run("bin/elasticsearch-plugin install analysis-nori")
//                                    .build()))
//                    .withEnv("discovery.type", "single-node")
//                    .withEnv("http.host", "0.0.0.0")
//                    .withFileSystemBind("./dict", "/usr/share/elasticsearch/config/dict", BindMode.READ_ONLY)
//                    .withExposedPorts(9200)
//                    .withReuse(true);
//
//            esContainer.start();
//            System.setProperty("spring.elasticsearch.rest.uris", esContainer.getHost());
//            String host = String.format("http://%s:%s", esContainer.getContainerIpAddress(), esContainer.getMappedPort(9200));
//
//            initialized = true;
//        }
//    }
//    @Override
//    public void afterAll(ExtensionContext extensionContext) throws Exception {
////        esContainer.stop();
//    }
//}