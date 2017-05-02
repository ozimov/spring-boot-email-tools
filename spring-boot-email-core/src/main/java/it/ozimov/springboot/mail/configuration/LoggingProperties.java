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

import it.ozimov.springboot.mail.logging.LoggingStrategy;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.__SPRING_MAIL_LOGGING;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = __SPRING_MAIL_LOGGING)
public class LoggingProperties {

    // spring.mail.logging.enabled
    private boolean enabled = true;

    // spring.mail.logging.strategy.*
    private Strategy strategy = new Strategy();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Strategy {

        // spring.mail.logging.strategy.from
        private LoggingStrategy from = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.replyTo
        private LoggingStrategy replyTo = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.to
        private LoggingStrategy to = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.cc
        private LoggingStrategy cc = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.bcc
        private LoggingStrategy bcc = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.subject
        private LoggingStrategy subject = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.body
        private LoggingStrategy body = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.attachments
        private LoggingStrategy attachments = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.encoding
        private LoggingStrategy encoding = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.locale
        private LoggingStrategy locale = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.sentAt
        private LoggingStrategy sentAt = LoggingStrategy.STANDARD_DATE_FORMAT;

        // spring.mail.logging.strategy.receiptTo
        private LoggingStrategy receiptTo = LoggingStrategy.PLAIN_TEXT;

        // spring.mail.logging.strategy.depositionNotificationTo
        private LoggingStrategy depositionNotificationTo = LoggingStrategy.PLAIN_TEXT;

        @Getter(AccessLevel.NONE)
        private Ignore ignore = new Ignore();

        public boolean areCustomHeadersIgnored() {
            return ignore.customHeaders;
        }

        public boolean areNullAndEmptyCollectionsIgnored() {
            return ignore.nullAndEmptyCollections;
        }

        @Data
        @NoArgsConstructor
        public static class Ignore {
            // spring.mail.logging.strategy.ignore.customHeaders
            private boolean customHeaders = true;

            private boolean nullAndEmptyCollections = true;

        }
    }

}