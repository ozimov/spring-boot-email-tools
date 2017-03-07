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

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class RedisBasedPersistenceServiceConstantsTest {

    private static final String EXPECTED_ORDERING_KEY_PREFIX = "priority-level:";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldBeUtilityClass() throws Exception {
        //Arrange
        Constructor<?> constructor = RedisBasedPersistenceServiceConstants.class.getDeclaredConstructor();
        assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
                .as("Constructor of an Utility Class should be private")
                .isTrue();
        constructor.setAccessible(true);

        expectedException.expectCause(
                allOf(instanceOf(UnsupportedOperationException.class),
                        hasProperty("message", equalTo("This is a utility class and cannot be instantiated"))
                ));

        //Act
        constructor.newInstance();
    }

    @Test
    public void shouldThrowExceptionWhenOrderingKeyIsConstructedFromNegativeNumber() throws Exception {
        //Arrange
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Priority level must be a positive integer number");

        //Act
        RedisBasedPersistenceServiceConstants.orderingKey(-1);

        //Assert
        fail();
    }


    @Test
    public void shouldThrowExceptionWhenOrderingKeyIsConstructedFromZero() throws Exception {
        //Arrange
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Priority level must be a positive integer number");

        //Act
        RedisBasedPersistenceServiceConstants.orderingKey(0);

        //Assert
        fail();
    }

    @Test
    public void shouldCreateOrderingKeyPrefixFromPositiveNumber() throws Exception {
        //Arrange
        int priorityLevel = 10;
        String expectedOrderingKey = EXPECTED_ORDERING_KEY_PREFIX + priorityLevel;

        //Act
        String givenOrderingKey = RedisBasedPersistenceServiceConstants.orderingKey(priorityLevel);

        //Assert
        assertions.assertThat(givenOrderingKey).isEqualTo(expectedOrderingKey);
    }

    @Test
    public void shouldOrderingKeyPrefixNotChange() {
        //Act
        final String givenPrefix = RedisBasedPersistenceServiceConstants.orderingKeyPrefix();

        //Assert
        assertions.assertThat(givenPrefix)
                .describedAs("Changing constants should be worth prison for life")
                .isEqualTo(EXPECTED_ORDERING_KEY_PREFIX);
    }

}