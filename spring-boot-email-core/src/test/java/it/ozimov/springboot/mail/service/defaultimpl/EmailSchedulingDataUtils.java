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

import com.google.common.collect.ImmutableMap;
import it.ozimov.springboot.mail.model.InlinePicture;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.mail.model.defaultimpl.TemplateEmailSchedulingData;
import it.ozimov.springboot.mail.utils.TimeUtils;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import static it.ozimov.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;

public class EmailSchedulingDataUtils {

    public static DefaultEmailSchedulingData createDefaultEmailSchedulingDataWithPriority(final int assignedPriority) throws UnsupportedEncodingException, InterruptedException {
        return createDefaultEmailSchedulingDataWithPriority(assignedPriority, 0);
    }

    public static DefaultEmailSchedulingData createDefaultEmailSchedulingDataWithPriority(final int assignedPriority, final long nanosFromNow) throws UnsupportedEncodingException, InterruptedException {
        TimeUnit.NANOSECONDS.sleep(1);
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow().plusNanos(nanosFromNow);

        final DefaultEmailSchedulingData defaultEmailSchedulingData = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(assignedPriority)
                .desiredPriority(assignedPriority)
                .build();
        return defaultEmailSchedulingData;
    }

    public static TemplateEmailSchedulingData createTemplateEmailSchedulingDataWithPriority(final int assignedPriority) throws UnsupportedEncodingException {
        return createTemplateEmailSchedulingDataWithPriority(assignedPriority, 0);
    }

    public static TemplateEmailSchedulingData createTemplateEmailSchedulingDataWithPriority(final int assignedPriority, final long nanosFromNow) throws UnsupportedEncodingException {
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow().plusNanos(nanosFromNow);

        final TemplateEmailSchedulingData templateEmailSchedulingData = TemplateEmailSchedulingData.templateEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .template("template.html")
                .modelObject(ImmutableMap.of("key", "var"))
                .inlinePictures(new InlinePicture[]{})
                .scheduledDateTime(dateTime)
                .assignedPriority(assignedPriority)
                .desiredPriority(assignedPriority)
                .build();
        return templateEmailSchedulingData;
    }

}