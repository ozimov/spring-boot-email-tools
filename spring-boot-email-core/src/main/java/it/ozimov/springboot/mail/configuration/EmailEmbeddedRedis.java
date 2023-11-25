/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.ozimov.springboot.mail.configuration;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

import jakarta.annotation.PreDestroy;
import java.util.HashMap;
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

        for (String setting : settings) {
            String normalizedSetting = setting.trim();
            int indexOfWhitespace = normalizedSetting.indexOf(WHITESPACE);
            if (indexOfWhitespace != -1 && indexOfWhitespace != normalizedSetting.length()) {
                String key = normalizedSetting.substring(0, indexOfWhitespace);
                String value = normalizedSetting.substring(indexOfWhitespace + 1).trim();
                keyValueMap.put(key, value);
            }
        }

        return keyValueMap.entrySet()
                .stream()
                .map(entry -> entry.getKey() + WHITESPACE + entry.getValue())
                .collect(toSet());
    }

}