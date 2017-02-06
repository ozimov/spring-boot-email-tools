package it.ozimov.springboot.templating.mail.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import javax.annotation.PreDestroy;

@Component
@ConditionalOnExpression(
        "'${spring.mail.persistence.enabled:false}' == 'true'" +
                " && '${spring.mail.persistence.redis.enabled:false}' == 'true'" +
                " && '${spring.mail.persistence.redis.embedded:false}' == 'true'")
@Slf4j
public class EmailEmbeddedRedis {

    @Getter
    private final int redisPort;

    private final RedisServer redisServer;

    @Autowired
    public EmailEmbeddedRedis(@Value("${spring.mail.persistence.redis.port}") final int redisPort) {
        this.redisPort = redisPort;
        redisServer = createStartedRedis();
    }

    private RedisServer createStartedRedis() {
        final RedisServer redisServer = RedisServer.builder()
                .port(redisPort)
                .setting("appendonly yes")
                .build();
        redisServer.start();
        log.info("Started Embedded Redis Server on port %d.", redisPort);
        return redisServer;
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
        log.info("Stopped Embedded Redis Server on port %d.", redisPort);
    }

    public boolean isActive() {
        return redisServer.isActive();
    }

}