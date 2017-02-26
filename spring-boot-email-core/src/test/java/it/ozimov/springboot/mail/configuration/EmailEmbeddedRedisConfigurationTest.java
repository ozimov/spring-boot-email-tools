package it.ozimov.springboot.mail.configuration;

import com.google.common.collect.ImmutableList;
import it.ozimov.springboot.mail.PortUtils;
import it.ozimov.springboot.mail.UnitTest;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;

public class EmailEmbeddedRedisConfigurationTest implements UnitTest {

    @Rule
    public final ExpectedException exportException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    private EmailEmbeddedRedisConfiguration emailEmbeddedRedisConfiguration;

    @Test
    public void shouldEmailEmbeddedRedisConfigurationBeCreate() throws Exception {
        //Arrange
        int port = 6381;
        List<String> settings = ImmutableList.of("appendfilename email_appendonly.aof",
                                                "save 900 1");

        //Act
        emailEmbeddedRedisConfiguration = new EmailEmbeddedRedisConfiguration(port, settings);

        //Assert
        assertions.assertThat(emailEmbeddedRedisConfiguration.emailEmbeddedRedis())
                .isNotNull();
        assertions.assertThat(emailEmbeddedRedisConfiguration.redisConnectionFactory())
                .isNotNull()
                .isInstanceOf(JedisConnectionFactory.class);



        //Act
        TimeUnit.SECONDS.sleep(3);
        emailEmbeddedRedisConfiguration.preDestroy();

        TimeUnit.SECONDS.sleep(1);
        //Assert
        assertions.assertThat(emailEmbeddedRedisConfiguration.emailEmbeddedRedis().isActive()).isFalse();

        exportException.expect(RedisConnectionFailureException.class);
        emailEmbeddedRedisConfiguration.redisConnectionFactory().getConnection();
    }
}