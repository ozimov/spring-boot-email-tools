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

package it.ozimov.springboot.templating.mail.utils;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.OffsetDateTime;

import static com.danhaywood.java.assertjext.Conditions.matchedBy;
import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static it.ozimov.cirneco.hamcrest.java7.base.IsBetweenInclusive.betweenInclusive;
import static it.ozimov.cirneco.hamcrest.java7.clazz.IsValidNoArgumentConstructor.hasNoArgumentConstructor;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public class TimeUtilsTest {

    @Rule
    public JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldHaveNoArgConstructor() throws Exception {
        //Assert
        assertions.assertThat(TimeUtilsTest.class)
                .is(matchedBy(hasNoArgumentConstructor()));
    }

    @Test
    public void shouldNowUsesUTC() throws Exception {
        //Arrange
        final long now = OffsetDateTime.now(UTC).toInstant().toEpochMilli();

        //Act
        final long actual = TimeUtils.now();

        //Assert
        assertions.assertThat(actual).isBetween(now, now + 2_000);
    }

    @Test
    public void shouldOffsetDateTimeNowUsesUTC() throws Exception {
        //Arrange
        final OffsetDateTime before = OffsetDateTime.now(UTC);

        //Act
        final OffsetDateTime actual = TimeUtils.offsetDateTimeNow();

        //Assert
        final OffsetDateTime after = OffsetDateTime.now(UTC);
        assertions.assertThat(actual)
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

}