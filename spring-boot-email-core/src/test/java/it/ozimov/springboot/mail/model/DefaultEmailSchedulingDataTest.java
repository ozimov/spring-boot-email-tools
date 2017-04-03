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

import it.ozimov.springboot.mail.UnitTest;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.mail.utils.TimeUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.time.OffsetDateTime;

import static it.ozimov.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;

public class DefaultEmailSchedulingDataTest implements UnitTest {

    @Rule
    public JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldCompareTo() throws Exception {
        //Arrange
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();
        final DefaultEmailSchedulingData reference = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(1)
                .desiredPriority(1)
                .build();
        final DefaultEmailSchedulingData smallerPrio = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(2)
                .desiredPriority(2)
                .build();
        final DefaultEmailSchedulingData before = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime.minusDays(1))
                .assignedPriority(1)
                .desiredPriority(1)
                .build();
        final DefaultEmailSchedulingData after = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime.plusDays(1))
                .assignedPriority(1)
                .desiredPriority(1)
                .build();

        //Act+Assert
        assertions.assertThat(reference).isLessThan(smallerPrio);
        assertions.assertThat(smallerPrio).isGreaterThan(reference);

        assertions.assertThat(reference).isGreaterThan(before);
        assertions.assertThat(before).isLessThan(reference);

        assertions.assertThat(reference).isLessThan(after);
        assertions.assertThat(after).isGreaterThan(reference);

        assertions.assertThat(smallerPrio).isLessThan(after);
        assertions.assertThat(after).isGreaterThan(smallerPrio);

        assertions.assertThat(smallerPrio).isGreaterThan(before);
        assertions.assertThat(before).isLessThan(smallerPrio);
    }

    @Test
    public void shouldGetScheduledDateTimeUseNowAsScheduledTime() throws Exception {
        //Arrange
        OffsetDateTime timeBefore = TimeUtils.offsetDateTimeNow();
        final DefaultEmailSchedulingData emailSchedulingData = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .assignedPriority(1)
                .desiredPriority(1)
                .build();
        OffsetDateTime timeAfter = TimeUtils.offsetDateTimeNow();

        //Act
        OffsetDateTime givenDefaultScheduledTime = emailSchedulingData.getScheduledDateTime();

        //Assert
        assertions.assertThat(givenDefaultScheduledTime)
                .isAfterOrEqualTo(timeBefore)
                .isBeforeOrEqualTo(timeAfter);
    }

}