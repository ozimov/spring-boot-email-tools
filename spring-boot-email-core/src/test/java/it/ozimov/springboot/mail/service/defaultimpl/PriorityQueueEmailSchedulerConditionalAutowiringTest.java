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
import it.ozimov.springboot.mail.service.EmailSchedulerService;
import it.ozimov.springboot.mail.service.EmailService;
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

import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;

import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.SPRING_MAIL_SCHEDULER_ENABLED;
import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.SPRING_MAIL_SCHEDULER_PRIORITY_LEVELS;

@RunWith(Enclosed.class)
public class PriorityQueueEmailSchedulerConditionalAutowiringTest {

    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(locations = "classpath:application.properties",
            properties =
                    {
                            SPRING_MAIL_SCHEDULER_ENABLED + "=false"
                    })
    public static class EmailSchedulerServiceDisabledContextBasedTest implements ContextBasedTest {

        @Rule
        public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

        @Rule
        public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

        @MockBean
        private EmailSchedulerProperties emailSchedulerProperties;

        @MockBean
        private EmailService emailService;

        @Mock
        private MimeMessage mimeMessage;

        @Autowired(required = false)
        private EmailSchedulerService emailSchedulerService;

        @Test
        public void shouldSchedulerServiceNotBeAutowired() throws Exception {
            //Assert
            assertions.assertThat(emailSchedulerService).isNull();
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
    public static class EmailEmailSchedulerPropertiesWithPersistenceContextBasedTest implements ContextBasedTest {

        @Rule
        public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

        @Rule
        public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

        @Autowired
        private EmailSchedulerProperties emailSchedulerProperties;

        @MockBean
        private EmailService emailService;

        @Mock
        private MimeMessage mimeMessage;

        @Autowired
        private EmailSchedulerService emailSchedulerService;

        @Test
        public void shouldSchedulerBeAutowired() throws Exception {
            //Assert
            assertions.assertThat(emailSchedulerService).isNotNull();
        }

    }

}