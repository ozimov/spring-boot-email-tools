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
import it.ozimov.springboot.mail.service.EmailService;
import it.ozimov.springboot.mail.service.SchedulerService;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;

import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.SPRING_MAIL_SCHEDULER_ENABLED;
import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.SPRING_MAIL_SCHEDULER_PRIORITY_LEVELS;

@RunWith(Enclosed.class)
public class PriorityQueueSchedulerConditionalAutowiringTest {

    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(locations = "classpath:application.properties",
            properties =
                    {
                            SPRING_MAIL_SCHEDULER_ENABLED + "=false"
                    })
    public static class SchedulerServiceDisabledContextBasedTest implements ContextBasedTest {

        @Rule
        public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

        @Rule
        public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

        @MockBean
        private SchedulerProperties schedulerProperties;

        @MockBean
        private EmailService emailService;

        @Mock
        private MimeMessage mimeMessage;

        @Autowired(required = false)
        private SchedulerService schedulerService;

        @Test
        public void shouldSchedulerServiceNotBeAutowired() throws Exception {
            //Assert
            assertions.assertThat(schedulerService).isNull();
        }

    }


    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(locations = "classpath:application.properties",
            properties =
                    {
                            SPRING_MAIL_SCHEDULER_ENABLED + "=true",
                            SPRING_MAIL_SCHEDULER_PRIORITY_LEVELS + "=10",

                    })
    public static class SchedulerPropertiesWithPersistenceContextBasedTest implements ContextBasedTest {

        @Rule
        public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

        @Rule
        public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

        @Autowired
        private SchedulerProperties schedulerProperties;

        @MockBean
        private EmailService emailService;

        @Mock
        private MimeMessage mimeMessage;

        @Autowired
        private SchedulerService schedulerService;

        @Test
        public void shouldSchedulerBeAutowired() throws Exception {
            //Assert
            assertions.assertThat(schedulerService).isNotNull();
        }

    }

}