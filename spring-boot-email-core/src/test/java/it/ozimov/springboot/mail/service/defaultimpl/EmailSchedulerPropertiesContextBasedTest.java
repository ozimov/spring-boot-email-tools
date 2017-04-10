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

package it.ozimov.springboot.mail.service.defaultimpl;

import it.ozimov.springboot.mail.ContextBasedTest;
import it.ozimov.springboot.mail.configuration.EmailSchedulerProperties;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.*;


@RunWith(Enclosed.class)
public class EmailSchedulerPropertiesContextBasedTest implements ContextBasedTest {

    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(locations = "classpath:application.properties",
            properties =
                    {
                            SPRING_MAIL_SCHEDULER_ENABLED + "=false"
                    })
    public static class SchedulerPropertiesWithoutEmailSchedulerContextBasedTest implements ContextBasedTest {

        @Rule
        public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

        @Rule
        public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

        @Autowired
        private EmailSchedulerProperties emailSchedulerProperties;

        @Test
        public void shouldSchedulerPropertiesHaveExpectedValues() throws Exception {
            //Assert
            assertions.assertThat(emailSchedulerProperties)
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("enabled", false)
                    .hasFieldOrPropertyWithValue("priorityLevels", null)
                    .hasFieldOrPropertyWithValue("persistence", null);
        }

    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(locations = "classpath:application.properties",
            properties =
                    {
                            SPRING_MAIL_SCHEDULER_ENABLED + "=true",
                            SPRING_MAIL_SCHEDULER_PRIORITY_LEVELS + "=123",
                            SPRING_MAIL_PERSISTENCE_ENABLED + "=false",
                            SPRING_MAIL_PERSISTENCE_ENABLED + "=false",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_EMBEDDED + "=false",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_ENABLED + "=false",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_DESIRED_BATCH_SIZE + "=1",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_MIN_KEPT_IN_MEMORY + "=1",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_MAX_KEPT_IN_MEMORY + "=1"
                    })
    public static class EmailSchedulerPropertiesWithoutPersistenceContextBasedTest implements ContextBasedTest {

        @Rule
        public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

        @Rule
        public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

        @Autowired
        private EmailSchedulerProperties emailSchedulerProperties;

        @Test
        public void shouldSchedulerPropertiesHaveExpectedValues() throws Exception {
            //Assert
            assertions.assertThat(emailSchedulerProperties)
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("enabled", true)
                    .hasFieldOrPropertyWithValue("priorityLevels", 123)
                    .hasFieldOrPropertyWithValue("persistence", EmailSchedulerProperties.Persistence.builder()
                            .desiredBatchSize(1).minKeptInMemory(1).maxKeptInMemory(1).build());
        }

    }


    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(locations = "classpath:application.properties",
            properties =
                    {
                            SPRING_MAIL_SCHEDULER_ENABLED + "=true",
                            SPRING_MAIL_SCHEDULER_PRIORITY_LEVELS + "=321",
                            SPRING_MAIL_PERSISTENCE_ENABLED + "=true",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_EMBEDDED + "=false",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_ENABLED + "=false",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_DESIRED_BATCH_SIZE + "=125",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_MIN_KEPT_IN_MEMORY + "=25",
                            SPRING_MAIL_SCHEDULER_PERSISTENCE_MAX_KEPT_IN_MEMORY + "=123456"
                    })
    public static class EmailSchedulerPropertiesWithPersistenceContextBasedTest implements ContextBasedTest {

        @Rule
        public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

        @Rule
        public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

        @Autowired
        private EmailSchedulerProperties emailSchedulerProperties;

        @Test
        public void shouldSchedulerPropertiesHaveExpectedValues() throws Exception {
            //Assert
            assertions.assertThat(emailSchedulerProperties)
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("enabled", true)
                    .hasFieldOrPropertyWithValue("priorityLevels", 321)
                    .hasFieldOrPropertyWithValue("persistence", EmailSchedulerProperties.Persistence.builder()
                            .desiredBatchSize(125).minKeptInMemory(25).maxKeptInMemory(123456).build());
        }

    }

}