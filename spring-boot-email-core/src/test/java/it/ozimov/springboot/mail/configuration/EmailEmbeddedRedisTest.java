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

import com.google.common.collect.ImmutableSet;
import it.ozimov.springboot.mail.UnitTest;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
public class EmailEmbeddedRedisTest implements UnitTest {

    private static final Character WHITESPACE = ' ';

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    private static final int PORT = 123;

    public EmailEmbeddedRedis emailEmbeddedRedis;

    @Test
    public void shouldGetSettingsReturnDefaultSettings() throws Exception {
        //Arrange
        emailEmbeddedRedis = new EmailEmbeddedRedis(PORT, ImmutableSet.of());

        //Act
        Set<String> givenSettings = emailEmbeddedRedis.getSettings();

        //Assert
        assertions.assertThat(givenSettings).containsOnly("appendonly yes", "appendfsync everysec");
    }

    @Test
    public void shouldGetSettingsReturnOverriddenDefaultSettings() throws Exception {
        //Arrange
        emailEmbeddedRedis = new EmailEmbeddedRedis(PORT, ImmutableSet.of("appendonly no", "appendfsync" + WHITESPACE + WHITESPACE + "always"));

        //Act
        Set<String> givenSettings = emailEmbeddedRedis.getSettings();

        //Assert
        assertions.assertThat(givenSettings).containsOnly("appendonly no", "appendfsync always");
    }

    @Test
    public void shouldGetSettingsReturnDefaultAndNewOnesSettings() throws Exception {
        //Arrange
        emailEmbeddedRedis = new EmailEmbeddedRedis(PORT, ImmutableSet.of("maxmemory 2mb"));

        //Act
        Set<String> givenSettings = emailEmbeddedRedis.getSettings();

        //Assert
        assertions.assertThat(givenSettings).containsOnly("appendonly yes", "appendfsync everysec", "maxmemory 2mb");
    }

}