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

import it.ozimov.springboot.mail.UnitTest;
import it.ozimov.springboot.mail.logging.LoggingStrategy;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class LoggingPropertiesTest implements UnitTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldReturnDefaultEnabled() throws Exception {
        //Arrange
        LoggingProperties loggingProperties = new LoggingProperties();

        //Act
        boolean givenEnabled = loggingProperties.isEnabled();

        //Assert
        assertions.assertThat(givenEnabled).isTrue();
    }

    @Test
    public void shouldReturnDefaultLoggingStrategyWhenNotProvided() throws Exception {
        //Arrange
        LoggingProperties loggingProperties = new LoggingProperties();

        //Act
        LoggingProperties.Strategy givenLoggingStrategy = loggingProperties.getStrategy();

        //Assert
        assertions.assertThat(givenLoggingStrategy.areCustomHeadersIgnored()).isTrue();
        assertions.assertThat(givenLoggingStrategy.areNullAndEmptyCollectionsIgnored()).isTrue();
        assertions.assertThat(givenLoggingStrategy.getFrom()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getReplyTo()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getTo()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getCc()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getBcc()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getSubject()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getBody()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getAttachments()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getEncoding()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getLocale()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getSentAt()).isEqualTo(LoggingStrategy.STANDARD_DATE_FORMAT);
        assertions.assertThat(givenLoggingStrategy.getReceiptTo()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getDepositionNotificationTo()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
    }

}