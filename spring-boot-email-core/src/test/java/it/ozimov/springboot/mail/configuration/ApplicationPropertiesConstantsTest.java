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

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.*;
import static org.hamcrest.Matchers.*;

public class ApplicationPropertiesConstantsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldBeUtilityClass() throws Exception {
        //Arrange
        Constructor<?> constructor = ApplicationPropertiesConstants.class.getDeclaredConstructor();
        assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
                .as("Constructor of an Utility Class should be private")
                .isTrue();
        constructor.setAccessible(true);

        expectedException.expectCause(
                allOf(instanceOf(UnsupportedOperationException.class),
                        hasProperty("message", equalTo("This is a utility class and cannot be instantiated"))
                ));

        //Act
        constructor.newInstance();
    }

    @Test
    public void shouldConstantsRemainUnchanged() {
        assertions.assertThat(__SPRING_MAIL_SCHEDULER).isEqualTo("spring.mail.scheduler");
        assertions.assertThat(SPRING_MAIL_HOST).isEqualTo("spring.mail.host");
        assertions.assertThat(SPRING_MAIL_PORT).isEqualTo("spring.mail.port");
        assertions.assertThat(SPRING_MAIL_USERNAME).isEqualTo("spring.mail.username");
        assertions.assertThat(SPRING_MAIL_PASSWORD).isEqualTo("spring.mail.password");
        assertions.assertThat(SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH).isEqualTo("spring.mail.properties.mail.smtp.auth");
        assertions.assertThat(SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE).isEqualTo("spring.mail.properties.mail.smtp.starttls.enable");
        assertions.assertThat(SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED).isEqualTo("spring.mail.properties.mail.smtp.starttls.required");
        assertions.assertThat(SPRING_MAIL_SCHEDULER_ENABLED).isEqualTo("spring.mail.scheduler.enabled");
        assertions.assertThat(SPRING_MAIL_SCHEDULER_PRIORITY_LEVELS).isEqualTo("spring.mail.scheduler.priorityLevels");
        assertions.assertThat(SPRING_MAIL_PERSISTENCE_ENABLED).isEqualTo("spring.mail.scheduler.persistence.enabled");
        assertions.assertThat(SPRING_MAIL_SCHEDULER_PERSISTENCE_DESIRED_BATCH_SIZE).isEqualTo("spring.mail.scheduler.persistence.desiredBatchSize");
        assertions.assertThat(SPRING_MAIL_SCHEDULER_PERSISTENCE_MIN_KEPT_IN_MEMORY).isEqualTo("spring.mail.scheduler.persistence.minKeptInMemory");
        assertions.assertThat(SPRING_MAIL_SCHEDULER_PERSISTENCE_MAX_KEPT_IN_MEMORY).isEqualTo("spring.mail.scheduler.persistence.maxKeptInMemory");
        assertions.assertThat(SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_ENABLED).isEqualTo("spring.mail.scheduler.persistence.redis.enabled");
        assertions.assertThat(SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_EMBEDDED).isEqualTo("spring.mail.scheduler.persistence.redis.embedded");
        assertions.assertThat(SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_HOST).isEqualTo("spring.mail.scheduler.persistence.redis.host");
        assertions.assertThat(SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_PORT).isEqualTo("spring.mail.scheduler.persistence.redis.port");
        assertions.assertThat(SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_SETTINGS).isEqualTo("spring.mail.scheduler.persistence.redis.settings");
        assertions.assertThat(SPRING_MAIL_LOGGING_ENABLED).isEqualTo("spring.mail.logging.enabled");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_FROM).isEqualTo("spring.mail.logging.strategy.from");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_REPLY_TO).isEqualTo("spring.mail.logging.strategy.replyTo");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_TO).isEqualTo("spring.mail.logging.strategy.to");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_CC).isEqualTo("spring.mail.logging.strategy.cc");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_BCC).isEqualTo("spring.mail.logging.strategy.bcc");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_SUBJECT).isEqualTo("spring.mail.logging.strategy.subject");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_BODY).isEqualTo("spring.mail.logging.strategy.body");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_ATTACHMENTS).isEqualTo("spring.mail.logging.strategy.attachments");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_ENCODING).isEqualTo("spring.mail.logging.strategy.encoding");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_LOCALE).isEqualTo("spring.mail.logging.strategy.locale");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_SENT_AT).isEqualTo("spring.mail.logging.strategy.sentAt");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_RECEIPT_TO).isEqualTo("spring.mail.logging.strategy.receiptTo");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_DEPOSITION_NOTIFICATION_TO).isEqualTo("spring.mail.logging.strategy.depositionNotificationTo");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_IGNORE_CUSTOM_HEADERS).isEqualTo("spring.mail.logging.strategy.ignore.customHeaders");
        assertions.assertThat(SPRING_MAIL_LOGGING_STRATEGY_IGNORE_NULL_AND_EMPTY_COLLECTIONS).isEqualTo("spring.mail.logging.strategy.ignore.nullAndEmptyCollections");
    }

}