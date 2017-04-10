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

import com.google.common.base.Preconditions;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.__SPRING_MAIL_SCHEDULER;
import static java.util.Objects.isNull;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = __SPRING_MAIL_SCHEDULER)
public class EmailSchedulerProperties {

    @Getter(AccessLevel.NONE)
    private boolean enabled;

    // spring.mail.scheduler.priorityLevels
    private Integer priorityLevels = 10;

    // spring.mail.scheduler.persistence.*
    private Persistence persistence = new Persistence();

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Persistence {

        // spring.mail.scheduler.persistence.desiredBatchSize
        private int desiredBatchSize = 500;

        // spring.mail.scheduler.persistence.minKeptInMemory
        private int minKeptInMemory = 250;

        // spring.mail.scheduler.persistence.maxKeptInMemory
        private int maxKeptInMemory = 2000;

    }

    @PostConstruct
    protected boolean validate() {
        if (enabled) {
            checkIsValid(this);
        } else {
            setValuesToNull();
        }
        return true;
    }

    public static void checkIsValid(@NonNull final EmailSchedulerProperties emailSchedulerProperties) {
        Preconditions.checkState(emailSchedulerProperties.getPriorityLevels() > 0,
                "Expected at least one priority level. Review property 'spring.mail.scheduler.priorityLevels'.");

        Preconditions.checkState(isNull(emailSchedulerProperties.getPersistence()) || emailSchedulerProperties.getPersistence().getDesiredBatchSize() > 0,
                "Expected at least a batch of size one, otherwise the persistence layer will not work. Review property 'spring.mail.scheduler.persistence.desiredBatchSize'.");

        Preconditions.checkState(isNull(emailSchedulerProperties.getPersistence()) || emailSchedulerProperties.getPersistence().getMinKeptInMemory() >= 0,
                "Expected a non negative amount of email to be kept in memory. Review property 'spring.mail.scheduler.persistence.minKeptInMemory'.");

        Preconditions.checkState(isNull(emailSchedulerProperties.getPersistence()) || emailSchedulerProperties.getPersistence().getMaxKeptInMemory() > 0,
                "Expected at least one email to be available in memory, otherwise the persistence layer will not work. Review property 'spring.mail.scheduler.persistence.maxKeptInMemory'.");

        Preconditions.checkState(isNull(emailSchedulerProperties.getPersistence()) ||
                        (emailSchedulerProperties.getPersistence().getMaxKeptInMemory() >= emailSchedulerProperties.getPersistence().getMinKeptInMemory()),
                "The application properties key '%s' should not have a value smaller than the value in property '%s'.",
                "spring.mail.scheduler.persistence.maxKeptInMemory", "spring.mail.scheduler.persistence.minKeptInMemory");

        Preconditions.checkState(isNull(emailSchedulerProperties.getPersistence()) ||
                        (emailSchedulerProperties.getPersistence().getMaxKeptInMemory() >= emailSchedulerProperties.getPersistence().getDesiredBatchSize()),
                "The application properties key '%s' should not have a value smaller than the value in property '%s'.",
                "spring.mail.scheduler.persistence.maxKeptInMemory", "spring.mail.scheduler.persistence.desiredBatchSize");
    }

    private void setValuesToNull() {
        priorityLevels = null;
        persistence = null;
    }

}