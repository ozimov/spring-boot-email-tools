package it.ozimov.springboot.templating.mail;

import it.ozimov.springboot.templating.mail.model.EmailSchedulingData;
import it.ozimov.springboot.templating.mail.service.EmailEmbeddedRedis;
import lombok.NonNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.util.SocketUtils;
import redis.clients.jedis.JedisShardInfo;
import redis.embedded.RedisServer;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@TestExecutionListeners(
        mergeMode =TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
        listeners = {BaseRedisTest.class})
public abstract class BaseRedisTest  implements ContextBasedTest, TestExecutionListener {

    protected AfterTransactionAssertion afterTransactionAssertion;
    protected BeforeTransactionAssertion beforeTransactionAssertion;

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

    @Autowired
    private RedisServer redisServer;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Before
    public void setUp(){
        cleanDataStore();
        additionalSetUp();
    }

    public final void cleanDataStore() {
        final RedisConnection connection = connectionFactory.getConnection();
        connection.flushDb();
        connection.close();
    }

    public void additionalSetUp(){
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

//    @
//    public void destroy() {
//        redisServer.stop();
////        if (Objects.nonNull(emailEmbeddedRedis)) {
////            emailEmbeddedRedis.stopRedis();
////        }
//    }

    @FunctionalInterface
    public interface BeforeTransactionAssertion {

        void assertBeforeTransaction(@NonNull final RedisConnection connection);
    }

    @FunctionalInterface
    public interface AfterTransactionAssertion {

        void assertAfterTransaction(@NonNull final RedisConnection connection);
    }


    @PreDestroy
    public void redisServerDestroy() {
        System.exit(-1);
    }

    @Configuration
    @PropertySource("classpath:redis-test.properties")
    //@TestPropertySource("classpath:redis-test.yml")
    @ComponentScan(basePackages = {"it.ozimov.springboot.templating.mail"})
    public static class JedisContextConfiguration {

        private EmailEmbeddedRedis emailEmbeddedRedis;
        private RedisServer redisServer;
        private JedisConnectionFactory connectionFactory;

        public JedisContextConfiguration() throws IOException {
            System.out.println("AAAAH");
            int redisPort = randomFreePort();

//            emailEmbeddedRedis = new EmailEmbeddedRedis(redisPort).startRedis();
            redisServer = RedisServer.builder()
                    .port(redisPort)
                    .setting("appendonly yes")
                    .build();
            redisServer.start();

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

//        @Bean
//        public PlatformTransactionManager transactionManager() throws SQLException {
//            return new DataSourceTransactionManager(dataSource);
//
////            return new AbstractPlatformTransactionManager() {
////                @Override
////                protected Object doGetTransaction() throws TransactionException {
////                    return null;
////                }
////
////                @Override
////                protected void doBegin(Object o, TransactionDefinition transactionDefinition) throws TransactionException {
////
////                }
////
////                @Override
////                protected void doCommit(DefaultTransactionStatus defaultTransactionStatus) throws TransactionException {
////
////                }
////
////                @Override
////                protected void doRollback(DefaultTransactionStatus defaultTransactionStatus) throws TransactionException {
////
////                }
////            };
//        }
//
//        DataSource dataSource = mock(DataSource.class);


        @Bean
        public DataSource dataSource() throws SQLException {
            DataSource dataSource = mock(DataSource.class);
//            when(dataSource.getConnection()).thenReturn(mock(Connection.class));
            return dataSource;
        }

        @Bean
        public EmailEmbeddedRedis emailEmbeddedRedis() throws IOException {
            return emailEmbeddedRedis;
        }

        private static int randomFreePort() throws IOException {
            return SocketUtils.findAvailableTcpPort();
        }

    }



    public void afterTestClass(TestContext testContext){
            redisServer.stop();
//            System.out.println("NOOOOOOOO");
//            if (nonNull(emailEmbeddedRedis)) {
//                emailEmbeddedRedis.stopRedis();
//            }
    }
    public void	afterTestMethod(TestContext testContext){}
    public void	beforeTestClass(TestContext testContext){}
    public void	beforeTestMethod(TestContext testContext){}
    public void	prepareTestInstance(TestContext testContext){}

}