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

package it.ozimov.springboot.mail.model.defaultimpl;

import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.EmailSchedulingData;
import it.ozimov.springboot.mail.utils.TimeUtils;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@EqualsAndHashCode(of = {"id", "scheduledDateTime", "assignedPriority", "desiredPriority"})
@ToString(of = {
        "id",
        "scheduledDateTime",
        "assignedPriority"
})
public class DefaultEmailSchedulingData implements EmailSchedulingData {

    private static final long serialVersionUID = 60021395842232155L;

    private final String id = UUID.randomUUID().toString();
    protected final Email email;
    protected final OffsetDateTime scheduledDateTime;
    protected final int assignedPriority;
    protected final int desiredPriority;

    @Builder(builderMethodName = "defaultEmailSchedulingDataBuilder")
    public DefaultEmailSchedulingData(@NonNull final Email email,
                                      @NonNull final OffsetDateTime scheduledDateTime,
                                      final int desiredPriority,
                                      final int assignedPriority) {
        checkArgument(assignedPriority > 0, "Priority cannot be less than 1");

        this.email = email;
        this.scheduledDateTime = scheduledDateTime;
        this.desiredPriority = desiredPriority;
        this.assignedPriority = assignedPriority;
    }

    public static class DefaultEmailSchedulingDataBuilder {
        protected OffsetDateTime scheduledDateTime = TimeUtils.offsetDateTimeNow();
    }

}