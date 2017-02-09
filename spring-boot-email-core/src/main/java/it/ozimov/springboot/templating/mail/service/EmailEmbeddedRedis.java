package it.ozimov.springboot.templating.mail.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import javax.annotation.PreDestroy;

import static it.ozimov.springboot.templating.mail.service.ApplicationPropertiesConstants.SPRING_MAIL_PERSISTENCE_REDIS_PORT;
import static it.ozimov.springboot.templating.mail.service.defaultimpl.ConditionalExpression.PERSISTENCE_WITH_EMBEDDED_REDIS_ENABLED_IS_TRUE;

@Component
@ConditionalOnExpression(PERSISTENCE_WITH_EMBEDDED_REDIS_ENABLED_IS_TRUE)
@Slf4j
public class EmailEmbeddedRedis {

    private static final String REDIS_PORT = "${" + SPRING_MAIL_PERSISTENCE_REDIS_PORT + "}";

    @Getter
    private final int redisPort;

    private final RedisServer redisServer;

    @Autowired
    public EmailEmbeddedRedis(@Value(REDIS_PORT) final int redisPort) {
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