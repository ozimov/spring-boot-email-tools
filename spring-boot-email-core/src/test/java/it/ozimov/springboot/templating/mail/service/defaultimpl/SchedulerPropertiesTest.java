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

package it.ozimov.springboot.templating.mail.service.defaultimpl;

import it.ozimov.springboot.templating.mail.UnitTest;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SchedulerPropertiesTest implements UnitTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldReturnDefaultPriorityLevelsWhenNotProvided() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = new SchedulerProperties();

        //Act
        int givenPriorityLevels = schedulerProperties.getPriorityLevels();

        //Assert
        assertions.assertThat(givenPriorityLevels).isEqualTo(10);
    }

    @Test
    public void shouldReturnDefaultPersistencePropertiesWhenNotProvided() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = new SchedulerProperties();

        //Act
        SchedulerProperties.Persistence givenPersistenceProperties = schedulerProperties.getPersistence();

        //Assert
        assertions.assertThat(givenPersistenceProperties.getDesiredBatchSize()).isEqualTo(500);
        assertions.assertThat(givenPersistenceProperties.getMinKeptInMemory()).isEqualTo(250);
        assertions.assertThat(givenPersistenceProperties.getMaxKeptInMemory()).isEqualTo(2000);
    }

    @Test
    public void shouldValidateReturnTrueWhenNumberOfPriorityLevelsIsPositiveAndSchedulerIsEnabled() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder()
                .enabled(true)
                .priorityLevels(1)
                .build();

        //Act
        boolean givenValidationResult = schedulerProperties.validate();

        //Arrange
        assertions.assertThat(givenValidationResult).isTrue();
    }

    @Test
    public void shouldValidateReturnTrueWhenSchedulerIsDisabledAndAllTheParamsAreWrong() throws Exception {
        //Arrange
        final int negativeInt = -100;
        assertions.assertThat(negativeInt).isNegative();

        SchedulerProperties schedulerProperties = SchedulerProperties.builder()
                .enabled(false)
                .priorityLevels(negativeInt)
                .persistence(SchedulerProperties.Persistence.builder()
                        .desiredBatchSize(negativeInt)
                        .minKeptInMemory(negativeInt)
                        .maxKeptInMemory(negativeInt)
                        .build())
                .build();

        //Act
        boolean givenValidationResult = schedulerProperties.validate();

        //Arrange
        assertions.assertThat(givenValidationResult).isTrue();
    }

    @Test
    public void shouldValidateThrowExceptionWhenNumberOfPriorityLevelsIsZeroAndSchedulerIsEnabled() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder().enabled(true).priorityLevels(0).build();

        expectedException.expect(IllegalStateException.class);

        //Act+Assert
        schedulerProperties.validate();
    }

    @Test
    public void shouldValidateThrowExceptionWhenNumberOfPriorityLevelsIsNegativeAndSchedulerIsEnabled() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder()
                .enabled(true)
                .priorityLevels(-1)
                .build();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Expected at least one priority level. Review property 'spring.mail.scheduler.priorityLevels'.");

        //Act+Assert
        schedulerProperties.validate();
    }


    @Test
    public void shouldValidateThrowExceptionWhenDesiredBatchSizeIsZeroAndSchedulerIsEnabled() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder()
                .enabled(true)
                .priorityLevels(1)
                .persistence(SchedulerProperties.Persistence.builder()
                        .desiredBatchSize(0)
                        .minKeptInMemory(100)
                        .maxKeptInMemory(10)
                        .build())
                .build();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Expected at least a batch of size one, otherwise the persistence layer will not work. Review property 'spring.mail.scheduler.persistence.desiredBatchSize'.");

        //Act+Assert
        schedulerProperties.validate();
    }

    @Test
    public void shouldValidateThrowExceptionWhenMaxKeptInMemoryIsNegativeAndSchedulerIsEnabled() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder()
                .enabled(true)
                .priorityLevels(1)
                .persistence(SchedulerProperties.Persistence.builder()
                        .desiredBatchSize(1)
                        .minKeptInMemory(-1)
                        .maxKeptInMemory(10)
                        .build())
                .build();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Expected a non negative amount of email to be kept in memory. Review property 'spring.mail.scheduler.persistence.minKeptInMemory'.");

        //Act+Assert
        schedulerProperties.validate();
    }

    @Test
    public void shouldValidateThrowExceptionWhenMaxKeptInMemoryIsZeroAndSchedulerIsEnabled() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder()
                .enabled(true)
                .priorityLevels(1)
                .persistence(SchedulerProperties.Persistence.builder()
                        .desiredBatchSize(1)
                        .minKeptInMemory(100)
                        .maxKeptInMemory(0)
                        .build())
                .build();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Expected at least one email to be available in memory, otherwise the persistence layer will not work. Review property 'spring.mail.scheduler.persistence.maxKeptInMemory'.");

        //Act+Assert
        schedulerProperties.validate();
    }

    @Test
    public void shouldValidateThrowExceptionWhenMaxInMemoryIsSmallerThanMinInMemoryAndSchedulerIsEnabled() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder()
                .enabled(true)
                .priorityLevels(1)
                .persistence(SchedulerProperties.Persistence.builder()
                        .desiredBatchSize(1)
                        .minKeptInMemory(100)
                        .maxKeptInMemory(10)
                        .build())
                .build();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("The application properties key 'spring.mail.scheduler.persistence.maxKeptInMemory' should not have a value smaller than the value in property 'spring.mail.scheduler.persistence.minKeptInMemory'.");

        //Act+Assert
        schedulerProperties.validate();
    }

    @Test
    public void shouldValidateThrowExceptionWhenMaxInMemoryIsSmallerThanDesiredBatchSizeAndSchedulerIsEnabled() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder()
                .enabled(true)
                .priorityLevels(1)
                .persistence(SchedulerProperties.Persistence.builder()
                        .desiredBatchSize(100)
                        .minKeptInMemory(1)
                        .maxKeptInMemory(10)
                        .build())
                .build();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("The application properties key 'spring.mail.scheduler.persistence.maxKeptInMemory' should not have a value smaller than the value in property 'spring.mail.scheduler.persistence.desiredBatchSize'.");

        //Act+Assert
        schedulerProperties.validate();
    }

}