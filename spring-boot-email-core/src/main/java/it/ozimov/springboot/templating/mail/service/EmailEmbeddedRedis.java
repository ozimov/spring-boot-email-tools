package it.ozimov.springboot.templating.mail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Component
@ConditionalOnProperty(prefix="spring.mail.persistence.redis", name={"enabled", "embedded"})
public class EmailEmbeddedRedis {

    private int redisPort;

    private RedisServer redisServer;

    @Autowired
    public EmailEmbeddedRedis(@Value("${spring.mail.persistence.redis.port}") final int redisPort) {
        this.redisPort = redisPort;
    }

    @PostConstruct
    public EmailEmbeddedRedis startRedis() throws IOException {
        redisServer = RedisServer.builder()
                .port(redisPort)
                .setting("appendonly yes")
                .build();
        redisServer.start();
        return this;
    }

    @PreDestroy
    public EmailEmbeddedRedis stopRedis() {
        redisServer.stop();
        return this;
    }

}