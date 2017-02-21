package it.ozimov.springboot.templating.mail.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

import javax.annotation.PreDestroy;
import java.util.Set;

@Slf4j
public class EmailEmbeddedRedis {

    @Getter
    private final int redisPort;
    @Getter
    private final Set<String> settings;

    private final RedisServer redisServer;

    public EmailEmbeddedRedis(final int redisPort, @NonNull final Set<String> settings) {
        this.redisPort = redisPort;
        this.settings = settings;
        redisServer = createStartedRedis();
    }

    private RedisServer createStartedRedis() {
        final RedisServerBuilder redisServerBuilder = RedisServer.builder()
                .port(redisPort)
                .setting("appendonly yes")
                .setting("appendfsync everysec")
//                .setting("save 1 1")
//                .setting("appendfilename email_appendonly.aof")
//                .setting("dbfilename email_dump.rdb")
//                .setting("dir /Users/trunfio/Downloads")
                ;
        settings.stream().forEach(s -> redisServerBuilder.setting(s));

        final RedisServer redisServer = redisServerBuilder.build();

        redisServer.start();
        log.info("Started Embedded Redis Server on port {}.", redisPort);
        return redisServer;
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
        log.info("Stopped Embedded Redis Server on port {}.", redisPort);
    }

    public boolean isActive() {
        return redisServer.isActive();
    }

}