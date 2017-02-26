package it.ozimov.springboot.mail.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisShardInfo;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;

import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_PORT;
import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_SETTINGS;
import static it.ozimov.springboot.mail.service.defaultimpl.ConditionalExpression.PERSISTENCE_IS_ENABLED_WITH_EMBEDDED_REDIS;
import static java.util.stream.Collectors.toSet;

@Configuration
@ConditionalOnExpression(PERSISTENCE_IS_ENABLED_WITH_EMBEDDED_REDIS)
@Slf4j
public class EmailEmbeddedRedisConfiguration {

    private static final String REDIS_PORT = "${" + SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_PORT + "}";

    private static final String REDIS_SETTINGS = "#{'${" + SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_SETTINGS + ":appendonly yes,appendfsync everysec}'.split(',')}";

    private EmailEmbeddedRedis emailEmbeddedRedis;
    private JedisConnectionFactory connectionFactory;

    public EmailEmbeddedRedisConfiguration(@Value(REDIS_PORT) final int redisPort,
                                           @Value(REDIS_SETTINGS) final List<String> redisSettings) throws IOException {

        emailEmbeddedRedis = new EmailEmbeddedRedis(redisPort,
                redisSettings.stream().map(s -> s.trim()).collect(toSet())
        ).start();

        JedisShardInfo shardInfo = new JedisShardInfo("localhost", redisPort);
        connectionFactory = new JedisConnectionFactory();
        connectionFactory.setShardInfo(shardInfo);
        connectionFactory.setUsePool(true);
        connectionFactory.getPoolConfig().setMaxTotal(10_000);
    }


    @Bean
    @ConditionalOnExpression(PERSISTENCE_IS_ENABLED_WITH_EMBEDDED_REDIS)
    public EmailEmbeddedRedis emailEmbeddedRedis() {
        return emailEmbeddedRedis;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return connectionFactory;
    }


    @PreDestroy
    public void preDestroy() {
        //Stopping REDIS
        emailEmbeddedRedis.stopRedis();
        //Stopping Jedis Connection Factory
        log.info("Destroying Jedis connection factory on host {} and port {}.", connectionFactory.getHostName(), connectionFactory.getPort());
        connectionFactory.destroy();
        log.info("Destroyed Jedis connection factory on host {} and port {}.", connectionFactory.getHostName(), connectionFactory.getPort());
    }

}
