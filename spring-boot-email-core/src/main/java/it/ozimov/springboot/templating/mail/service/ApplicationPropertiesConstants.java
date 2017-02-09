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

package it.ozimov.springboot.templating.mail.service;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApplicationPropertiesConstants {

    private static final String DOT = ".";

    public static final String __SPRING_MAIL_SCHEDULER = "spring.mail.scheduler";

    public static final String SPRING_MAIL_HOST = "spring.mail.host";

    public static final String SPRING_MAIL_PORT = "spring.mail.port";

    public static final String SPRING_MAIL_USERNAME = "spring.mail.username";

    public static final String SPRING_MAIL_PASSWORD = "spring.mail.password";

    public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH = "spring.mail.properties.mail.smtp.auth";

    public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE = "spring.mail.properties.mail.smtp.starttls.enable";

    public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED = "spring.mail.properties.mail.smtp.starttls.required";

    public static final String SPRING_MAIL_SCHEDULER_PRIORITY_LEVELS = __SPRING_MAIL_SCHEDULER + DOT + "priorityLevels";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_LAYER_DESIRED_BATCH_SIZE = __SPRING_MAIL_SCHEDULER + DOT + "persistenceLayer.desiredBatchSize";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_LAYER_MIN_KEPT_IN_MEMORY = __SPRING_MAIL_SCHEDULER + DOT + "persistenceLayer.minKeptInMemory";

    public static final String SPRING_MAIL_SCHEDULER_PERSISTENCE_LAYER_MAX_KEPT_IN_MEMORY = __SPRING_MAIL_SCHEDULER + DOT + "persistenceLayer.maxKeptInMemory";

    public static final String SPRING_MAIL_PERSISTENCE_ENABLED = "spring.mail.persistence.enabled";

    public static final String SPRING_MAIL_PERSISTENCE_REDIS_ENABLED = "spring.mail.persistence.redis.enabled";

    public static final String SPRING_MAIL_PERSISTENCE_REDIS_EMBEDDED = "spring.mail.persistence.redis.embedded";

    public static final String SPRING_MAIL_PERSISTENCE_REDIS_HOST = "spring.mail.persistence.redis.host";

    public static final String SPRING_MAIL_PERSISTENCE_REDIS_PORT = "spring.mail.persistence.redis.port";

}
