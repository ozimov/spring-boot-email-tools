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

package it.ozimov.springboot.mail.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@Builder
public class EmailSchedulingWrapper implements Comparable<EmailSchedulingWrapper> {

    private final UUID id = UUID.randomUUID();
    private final Email email;
    private final OffsetDateTime scheduledDateTime;
    private final int priority;

    public EmailSchedulingWrapper(@NonNull final Email email, @NonNull final OffsetDateTime scheduledDateTime, final int priority) {
        checkArgument(priority > 0, "Priority cannot be less than 1");

        this.email = email;
        this.scheduledDateTime = scheduledDateTime;
        this.priority = priority;
    }

    @Override
    public int compareTo(EmailSchedulingWrapper o) {
        if (scheduledDateTime.isBefore(o.getScheduledDateTime())) {
            return -1;
        } else if (scheduledDateTime.isAfter(o.getScheduledDateTime())) {
            return 1;
        } else {
            return Integer.compare(priority, o.getPriority());
        }
    }

}
