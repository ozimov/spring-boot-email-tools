package it.ozimov.springboot.templating.mail.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Component(value = "EmailEmbeddedRedis")
@ConditionalOnProperty("${spring.mail.persistence.redis.enabled:false}")
public class EmbeddedRedis {

    private final int redisPort;

    private RedisServer redisServer;

    public EmbeddedRedis(@Value("${spring.mail.persistence.redis.port:6381}") final int redisPort) {
        this.redisPort = redisPort;
    }

    @PostConstruct
    public void startRedis() throws IOException {
        redisServer = RedisServer.builder()
                .port(redisPort)
                .setting("appendonly yes")
                .build();
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
    }

}