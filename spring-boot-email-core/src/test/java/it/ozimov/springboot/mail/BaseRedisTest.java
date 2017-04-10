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

package it.ozimov.springboot.mail;

import com.google.common.collect.ImmutableSet;
import it.ozimov.springboot.mail.configuration.EmailEmbeddedRedis;
import it.ozimov.springboot.mail.configuration.EmailSchedulerProperties;
import it.ozimov.springboot.mail.model.EmailSchedulingData;
import lombok.NonNull;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.JedisShardInfo;
import redis.embedded.RedisServer;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

import static it.ozimov.springboot.mail.PortUtils.randomFreePort;
import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.*;
import static java.util.Objects.nonNull;
import static org.mockito.Mockito.mock;

public abstract class BaseRedisTest implements ContextBasedTest {

    protected AfterTransactionAssertion afterTransactionAssertion;
    protected BeforeTransactionAssertion beforeTransactionAssertion;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Before
    public void setUp() {
        cleanDataStore();
    }

    public final void cleanDataStore() {
        final RedisConnection connection = connectionFactory.getConnection();
        connection.flushDb();
        connection.close();
    }


    protected AfterTransactionAssertion getAfterTransactionAssertion() {
        return afterTransactionAssertion;
    }

    protected BeforeTransactionAssertion getBeforeTransactionAssertion() {
        return beforeTransactionAssertion;
    }

    protected void setAfterTransactionAssertion(@NonNull final AfterTransactionAssertion afterTransactionAssertion) {
        this.afterTransactionAssertion = afterTransactionAssertion;
    }

    protected void setBeforeTransactionAssertion(@NonNull final BeforeTransactionAssertion beforeTransactionAssertion) {
        this.beforeTransactionAssertion = beforeTransactionAssertion;
    }

    @BeforeTransaction
    public void assertBeforeTransaction() {
        if (nonNull(beforeTransactionAssertion)) {
            final RedisConnection connection = connectionFactory.getConnection();
            beforeTransactionAssertion.assertBeforeTransaction(connection);
            connection.close();
        }
    }

    @AfterTransaction
    public void assertAfterTransaction() {
        if (nonNull(afterTransactionAssertion)) {
            final RedisConnection connection = connectionFactory.getConnection();
            afterTransactionAssertion.assertAfterTransaction(connection);
            connection.close();
        }
    }

    @FunctionalInterface
    public interface BeforeTransactionAssertion {

        void assertBeforeTransaction(@NonNull final RedisConnection connection);
    }

    @FunctionalInterface
    public interface AfterTransactionAssertion {

        void assertAfterTransaction(@NonNull final RedisConnection connection);
    }

    @Configuration
    @TestPropertySource(locations = "classpath:application.properties",
            properties =
                    {
                            SPRING_MAIL_SCHEDULER_ENABLED + "=true",
                            SPRING_MAIL_SCHEDULER_PRIORITY_LEVELS + "=321",
                            SPRING_MAIL_PERSISTENCE_ENABLED + "=true",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_EMBEDDED + "=false",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_ENABLED + "=false",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_HOST + "=localhost",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_PORT + "=6381",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_DESIRED_BATCH_SIZE + "=125",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_MIN_KEPT_IN_MEMORY + "=25",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_MAX_KEPT_IN_MEMORY + "=123456"
                    })
    @ComponentScan(basePackages = {"it.ozimov.springboot.mail"})
    public static class ContextConfiguration {

        private EmailEmbeddedRedis emailEmbeddedRedis;
        private RedisServer redisServer;
        private JedisConnectionFactory connectionFactory;

        public ContextConfiguration() throws IOException {
            int redisPort = randomFreePort();

            emailEmbeddedRedis = new EmailEmbeddedRedis(redisPort, ImmutableSet.of()).start();

            redisServer = (RedisServer) ReflectionTestUtils.getField(emailEmbeddedRedis, "redisServer");

            JedisShardInfo shardInfo = new JedisShardInfo("localhost", redisPort);
            connectionFactory = new JedisConnectionFactory();
            connectionFactory.setShardInfo(shardInfo);
            connectionFactory.setUsePool(true);
            connectionFactory.getPoolConfig().setMaxTotal(10_000);
        }

        @Bean
        public PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public RedisServer redisServer() {
            return redisServer;
        }

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return connectionFactory;
        }

        @Bean("orderingTemplate")
        public StringRedisTemplate createOrderingTemplate() throws IOException {
            StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
            template.setEnableTransactionSupport(true);
            return template;
        }

        @Bean("valueTemplate")
        public RedisTemplate<String, EmailSchedulingData> createValueTemplate() throws IOException {
            RedisTemplate<String, EmailSchedulingData> template = new RedisTemplate<>();
            RedisSerializer<String> stringSerializer = new StringRedisSerializer();
            JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
            template.setKeySerializer(stringSerializer);
            template.setValueSerializer(jdkSerializationRedisSerializer);
            template.setHashKeySerializer(stringSerializer);
            template.setHashValueSerializer(stringSerializer);

            template.setConnectionFactory(connectionFactory);
            template.setEnableTransactionSupport(true);
            template.afterPropertiesSet();
            return template;
        }

        @Bean
        public DataSource dataSource() throws SQLException {
            DataSource dataSource = mock(DataSource.class);
            return dataSource;
        }

        @Bean
        public EmailEmbeddedRedis emailEmbeddedRedis() throws IOException {
            return emailEmbeddedRedis;
        }

        @Bean
        public EmailSchedulerProperties emailSchedulerProperties() {
            return EmailSchedulerProperties.builder()
                    .priorityLevels(1)
                    .persistence(EmailSchedulerProperties.Persistence.builder()
                            .desiredBatchSize(1)
                            .maxKeptInMemory(1)
                            .build())
                    .build();
        }

    }

}