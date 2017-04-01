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

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApplicationPropertiesConstants {

    private static final String DOT = ".";

    public static final String __SPRING_MAIL_SCHEDULER = "spring.mail.scheduler";

    public static final String __SPRING_MAIL_LOGGING = "spring.mail.logging";

    public static final String __SPRING_MAIL_LOGGING_STRATEGY = __SPRING_MAIL_LOGGING + DOT + "strategy";

    public static final String SPRING_MAIL_HOST = "spring.mail.host";

    public static final String SPRING_MAIL_PORT = "spring.mail.port";

    public static final String SPRING_MAIL_USERNAME = "spring.mail.username";

    public static final String SPRING_MAIL_PASSWORD = "spring.mail.password";

    public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH = "spring.mail.properties.mail.smtp.auth";

    public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE = "spring.mail.properties.mail.smtp.starttls.enable";

    public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED = "spring.mail.properties.mail.smtp.starttls.required";

    public static final String SPRING_MAIL_SCHEDULER_ENABLED = __SPRING_MAIL_SCHEDULER + DOT + "enabled";

    public static final String SPRING_MAIL_SCHEDULER_PRIORITY_LEVELS = __SPRING_MAIL_SCHEDULER + DOT + "priorityLevels";

    public static final String SPRING_MAIL_PERSISTENCE_ENABLED = __SPRING_MAIL_SCHEDULER + DOT + "persistence.enabled";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_DESIRED_BATCH_SIZE = __SPRING_MAIL_SCHEDULER + DOT + "persistence.desiredBatchSize";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_MIN_KEPT_IN_MEMORY = __SPRING_MAIL_SCHEDULER + DOT + "persistence.minKeptInMemory";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_MAX_KEPT_IN_MEMORY = __SPRING_MAIL_SCHEDULER + DOT + "persistence.maxKeptInMemory";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_ENABLED = __SPRING_MAIL_SCHEDULER + DOT + "persistence.redis.enabled";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_EMBEDDED = __SPRING_MAIL_SCHEDULER + DOT + "persistence.redis.embedded";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_HOST = __SPRING_MAIL_SCHEDULER + DOT + "persistence.redis.host";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_PORT = __SPRING_MAIL_SCHEDULER + DOT + "persistence.redis.port";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_SETTINGS = __SPRING_MAIL_SCHEDULER + DOT + "persistence.redis.settings";

    public static final String SPRING_MAIL_LOGGING_ENABLED = __SPRING_MAIL_LOGGING + DOT + "enabled";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_FROM = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "from";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_REPLY_TO = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "replyTo";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_TO = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "to";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_CC = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "cc";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_BCC = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "bcc";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_SUBJECT = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "subject";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_BODY = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "body";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_ATTACHMENTS = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "attachments";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_ENCODING = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "encoding";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_LOCALE = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "locale";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_SENT_AT = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "sentAt";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_RECEIPT_TO = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "receiptTo";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_DEPOSITION_NOTIFICATION_TO = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "depositionNotificationTo";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_IGNORE_CUSTOM_HEADERS = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "ignore.customHeaders";

    public static final String SPRING_MAIL_LOGGING_STRATEGY_IGNORE_NULL_AND_EMPTY_COLLECTIONS = __SPRING_MAIL_LOGGING_STRATEGY + DOT + "ignore.nullAndEmptyCollections";

}
