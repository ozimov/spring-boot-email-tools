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

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SchedulerPropertiesTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldReturnDefaultPriorityLevelsWhenNotProvided() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder().build();

        //Act
        int givenPriorityLevels = schedulerProperties.getPriorityLevels();

        //Assert
        assertions.assertThat(givenPriorityLevels).isEqualTo(10);
    }

    @Test
    public void shouldReturnDefaultPersistenceLayerPropertiesWhenNotProvided() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder().priorityLevels(1).build();

        //Act
        SchedulerProperties.PersistenceLayer givenPersistenceLayerProperties = schedulerProperties.getPersistenceLayer();

        //Assert
        assertions.assertThat(givenPersistenceLayerProperties.getDesiredBatchSize()).isEqualTo(500);
        assertions.assertThat(givenPersistenceLayerProperties.getMaxKeptInMemory()).isEqualTo(2000);
    }

    @Test
    public void shouldValidateReturnTrueWhenNumberOfPriorityLevelsIsPositive() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder().priorityLevels(1).build();

        //Act
        boolean givenValidationResult = schedulerProperties.validate();

        //Arrange
        assertions.assertThat(givenValidationResult).isTrue();
    }

    @Test
    public void shouldValidateThrowExceptionWhenNumberOfPriorityLevelsIsZero() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder().priorityLevels(0).build();

        expectedException.expect(IllegalStateException.class);

        //Act+Assert
        schedulerProperties.validate();
    }

    @Test
    public void shouldValidateThrowExceptionWhenNumberOfPriorityLevelsIsNegative() throws Exception {
        //Arrange
        SchedulerProperties schedulerProperties = SchedulerProperties.builder().priorityLevels(-1).build();

        expectedException.expect(IllegalStateException.class);

        //Act+Assert
        schedulerProperties.validate();
    }

}