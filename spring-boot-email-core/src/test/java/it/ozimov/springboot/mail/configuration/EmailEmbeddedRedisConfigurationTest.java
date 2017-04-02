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

import com.google.common.collect.ImmutableList;
import it.ozimov.springboot.mail.UnitTest;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EmailEmbeddedRedisConfigurationTest implements UnitTest {

    @Rule
    public final ExpectedException exportException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldEmailEmbeddedRedisConfigurationBeCreated() throws Exception {
        //Arrange
        int port = 6381;
        List<String> settings = ImmutableList.of("appendfilename email_appendonly.aof",
                "save 900 1");

        //Act
        EmailEmbeddedRedisConfiguration emailEmbeddedRedisConfiguration = new EmailEmbeddedRedisConfiguration(port, settings);

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