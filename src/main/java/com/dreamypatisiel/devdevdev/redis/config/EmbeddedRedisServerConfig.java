package com.dreamypatisiel.devdevdev.redis.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import redis.embedded.RedisServer;

/**
 * <a href="https://jojoldu.tistory.com/297">참고</a>
 */
@Slf4j
@Configuration
@Profile({"test", "local"})
public class EmbeddedRedisServerConfig {

    private static final int START_PORT = 10_000;
    private static final int END_PORT = 65_535;
    private static final String FIND_PORT_COMMAND = "netstat -nat | grep LISTEN|grep %d";
    private static final String BASH = "/bin/sh";
    private static final String C = "-c";
    private static final String NOT_FOUND_AVAILABLE_PORT_MESSAGE = "Not Found Available port: %d ~ %d";
    private static final String REDIS_SERVER_START_MESSAGE = "redisServer start..={}";
    private static final String REDIS_SERVER_STOP_MESSAGE = "redisServer stop..";

    @Value("${spring.data.redis.port}")
    private int redisPort;
    private RedisServer redisServer;

    @PostConstruct
    public void runRedis() throws IOException {
        int port = isRedisRunning() ? findAvailablePort() : redisPort;
        if (isArmMac()) {
            redisServer = new RedisServer(getRedisFileForArcMac(), port);
        } else if (isX86Mac()) {
            redisServer = new RedisServer(getRedisFileForX86Mac(), port);
        } else {
            redisServer = RedisServer.builder()
                    .port(port)
                    .setting("maxmemory 128MB")
                    .build();
        }

        redisServer.start();

        log.info(REDIS_SERVER_START_MESSAGE, port);
    }

    @PreDestroy
    public void stopRedis() {
        if (!ObjectUtils.isEmpty(redisServer)) {
            redisServer.stop();
            log.info(REDIS_SERVER_STOP_MESSAGE);
        }
    }

    /**
     * Embedded Redis가 현재 실행중인지 확인
     */
    private boolean isRedisRunning() throws IOException {
        return isRunning(executeGrepProcessCommand(redisPort));
    }

    /**
     * 현재 PC/서버에서 사용가능한 포트 조회
     */
    public int findAvailablePort() throws IOException {

        for (int port = START_PORT; port <= END_PORT; port++) {
            Process process = executeGrepProcessCommand(port);
            if (!isRunning(process)) {
                return port;
            }
        }

        throw new IllegalArgumentException(String.format(NOT_FOUND_AVAILABLE_PORT_MESSAGE, START_PORT, END_PORT));
    }

    /**
     * 해당 port를 사용중인 프로세스 확인하는 sh 실행
     */
    private Process executeGrepProcessCommand(int port) throws IOException {
        String command = String.format(FIND_PORT_COMMAND, port);
        String[] shell = {BASH, C, command};

        return Runtime.getRuntime().exec(shell);
    }

    /**
     * 해당 Process가 현재 실행중인지 확인
     */
    private boolean isRunning(Process process) {
        String line;
        StringBuilder pidInfo = new StringBuilder();

        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            while ((line = input.readLine()) != null) {
                pidInfo.append(line);
            }

        } catch (Exception e) {
        }

        return !StringUtils.isEmpty(pidInfo.toString());
    }

    /**
     * 현재 시스템이 ARM 아키텍처를 사용하는 MAC인지 확인 System.getProperty("os.arch") : JVM이 실행되는 시스템 아키텍처 반환
     * System.getProperty("os.name") : 시스템 이름 반환
     */
    private boolean isArmMac() {
        return Objects.equals(System.getProperty("os.arch"), "aarch64")
                && Objects.equals(System.getProperty("os.name"), "Mac OS X");
    }

    /**
     * 현재 시스템이 X86 아키텍처를 사용하는 MAC인지 확인 System.getProperty("os.arch") : JVM이 실행되는 시스템 아키텍처 반환
     * System.getProperty("os.name") : 시스템 이름 반환
     */
    private boolean isX86Mac() {
        return Objects.equals(System.getProperty("os.arch"), "x86_64")
                && Objects.equals(System.getProperty("os.name"), "Mac OS X");
    }

    /**
     * ARM 아키텍처를 사용하는 Mac에서 실행할 수 있는 Redis 바이너리 파일을 반환
     */
    private File getRedisFileForArcMac() {
        try {
            return new ClassPathResource("redis/redis-server-mac-arm64").getFile();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * X86 아키텍처를 사용하는 Mac에서 실행할 수 있는 Redis 바이너리 파일을 반환
     */
    private File getRedisFileForX86Mac() {
        try {
            return new ClassPathResource("redis/redis-server-mac-x86").getFile();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
