package it.ozimov.springboot.templating.mail.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
public class EmailEmbeddedRedis {

    private static final Character WHITESPACE = ' ';

    @Getter
    private final int redisPort;
    @Getter
    private final Set<String> settings;

    private final RedisServer redisServer;

    public EmailEmbeddedRedis(final int redisPort, @NonNull final Set<String> settings) {
        this.redisPort = redisPort;
        this.settings = mergeSettings(settings);
        redisServer = createRedisServer();
    }

    private RedisServer createRedisServer() {
        final RedisServerBuilder redisServerBuilder = RedisServer.builder()
                .port(redisPort)
                .setting("appendonly yes")
                .setting("appendfsync everysec");
        settings.stream().forEach(s -> redisServerBuilder.setting(s));

        final RedisServer redisServer = redisServerBuilder.build();
        return redisServer;
    }


    public EmailEmbeddedRedis start() {
        log.info("Started Embedded Redis Server on port {}.", redisPort);
        redisServer.start();
        return this;
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
        log.info("Stopped Embedded Redis Server on port {}.", redisPort);
    }

    public boolean isActive() {
        return redisServer.isActive();
    }

    private Set<String> mergeSettings(final Set<String> settings) {
        Map<String, String> keyValueMap = new HashMap<>();
        keyValueMap.put("appendonly", "yes");
        keyValueMap.put("appendfsync", "everysec");

        for(String setting : settings){
            String normalizedSetting = setting.trim();
            int indexOfWhitespace =normalizedSetting.indexOf(WHITESPACE);
            if(indexOfWhitespace != -1 && indexOfWhitespace != normalizedSetting.length()) {
                String key = normalizedSetting.substring(0, indexOfWhitespace);
                String value = normalizedSetting.substring(indexOfWhitespace+1).trim();
                keyValueMap.put(key, value);
            }
        }

        return keyValueMap.entrySet()
                .stream()
                .map(entry -> entry.getKey()+WHITESPACE+entry.getValue())
                .collect(toSet());
    }

}