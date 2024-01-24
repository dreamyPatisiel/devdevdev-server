package com.dreamypatisiel.devdevdev.global.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import redis.embedded.RedisServer;

/**
 * <a href="https://jojoldu.tistory.com/297">참고</a>
 */
@Slf4j
@Configuration
@Profile({"test", "local"})
public class EmbeddedRedisConfig {

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
        redisServer = new RedisServer(port);
        redisServer.start();
        log.info(REDIS_SERVER_START_MESSAGE, port);
    }

    @PreDestroy
    public void stopRedis() {
        if(!ObjectUtils.isEmpty(redisServer)) {
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

        } catch (Exception e) {}

        return !StringUtils.isEmpty(pidInfo.toString());
    }
}
