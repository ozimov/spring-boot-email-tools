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

import it.ozimov.springboot.mail.BaseRedisTest;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import redis.embedded.RedisServer;
import redis.embedded.exceptions.EmbeddedRedisException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseRedisTest.ContextConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmailEmbeddedRedisContextTest extends BaseRedisTest {

    private static final String REDIS_SERVER_FIELD_NAME = "redisServer";

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Autowired
    public EmailEmbeddedRedis emailEmbeddedRedis;

    private RedisServer redisServerOriginal;

    @Before
    public void copyOriginalRedisServer() {
        redisServerOriginal = (RedisServer) ReflectionTestUtils.getField(emailEmbeddedRedis, REDIS_SERVER_FIELD_NAME);
    }

    @After
    public void restoreOriginalRedisServer() {
        ReflectionTestUtils.setField(emailEmbeddedRedis, REDIS_SERVER_FIELD_NAME, redisServerOriginal);
    }

    @Test
    public void shouldRedisHaveBeenStarted() throws Exception {
        //Assert
        assertions.assertThat(redisServerOriginal.ports()).containsOnly(emailEmbeddedRedis.getRedisPort());
        assertions.assertThat(redisServerOriginal.isActive()).isTrue();
    }

    @Test
    public void shouldStopRedis() throws Exception {
        //Arrange
        final RedisServer redisServerMock = mock(ForTestRedisServer.class);
        ReflectionTestUtils.setField(emailEmbeddedRedis, REDIS_SERVER_FIELD_NAME, redisServerMock);
        when(redisServerMock.isActive()).thenReturn(false);

        //Act
        emailEmbeddedRedis.stopRedis();

        //Assert
        verify(redisServerMock).stop();
        assertions.assertThat(emailEmbeddedRedis.isActive()).isFalse();
    }

    public class ForTestRedisServer extends RedisServer {

        public ForTestRedisServer() throws IOException {
        }

        public synchronized void stop() throws EmbeddedRedisException {
            //Do nothing
        }

        public boolean isActive() {
            throw new UnsupportedOperationException();
        }

    }

}