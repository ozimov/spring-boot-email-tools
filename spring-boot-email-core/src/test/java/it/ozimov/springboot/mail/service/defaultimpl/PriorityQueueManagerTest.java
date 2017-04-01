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

import it.ozimov.springboot.mail.model.EmailSchedulingData;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailSchedulingData;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.TreeSet;

import static it.ozimov.springboot.mail.service.defaultimpl.EmailSchedulingDataUtils.createDefaultEmailSchedulingDataWithPriority;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;

public class PriorityQueueManagerTest {

    @Rule
    public final Timeout timeout = new Timeout(10, SECONDS);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    private int numberOfPriorityLevels = 5;
    private boolean hasPersistence = false;
    private int maxInMemory = Integer.MAX_VALUE;
    private Duration queuabilityDelta = Duration.ZERO;

    private PriorityQueueManager priorityQueueManager;

    @Before
    public void setUp() {
        priorityQueueManager = spy(new PriorityQueueManager(numberOfPriorityLevels, hasPersistence, maxInMemory, queuabilityDelta));
    }

    @Test
    public void shouldConstructorThrowExceptionGivenNegativeNumberOfPriorityLevels() throws Exception {
        //Arrange
        numberOfPriorityLevels = -1;

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(String.format("Number of priority levels should be a positive number, while %s was given", numberOfPriorityLevels));

        //Act
        new PriorityQueueManager(numberOfPriorityLevels, hasPersistence, maxInMemory, queuabilityDelta);

        //Assert
        fail();
    }

    @Test
    public void shouldConstructorThrowExceptionGivenZeroNumberOfPriorityLevels() throws Exception {
        //Arrange
        numberOfPriorityLevels = 0;

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(String.format("Number of priority levels should be a positive number, while %s was given", numberOfPriorityLevels));

        //Act
        new PriorityQueueManager(numberOfPriorityLevels, hasPersistence, maxInMemory, queuabilityDelta);

        //Assert
        fail();
    }

    @Test
    public void shouldConstructorThrowExceptionGivenNegativeMaxInMemory() throws Exception {
        //Arrange
        maxInMemory = -1;

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(String.format("Number of max emails in memory should be a positive number, while %s was given", maxInMemory));

        //Act
        new PriorityQueueManager(numberOfPriorityLevels, hasPersistence, maxInMemory, queuabilityDelta);

        //Assert
        fail();
    }

    @Test
    public void shouldConstructorThrowExceptionGivenZeroMaxInMemory() throws Exception {
        //Arrange
        maxInMemory = 0;

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(String.format("Number of max emails in memory should be a positive number, while %s was given", maxInMemory));

        //Act
        new PriorityQueueManager(numberOfPriorityLevels, hasPersistence, maxInMemory, queuabilityDelta);

        //Assert
        fail();
    }

    @Test
    public void shouldConstructorThrowExceptionGivenNullDuration() throws Exception {
        //Arrange
        queuabilityDelta = null;

        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("queuabilityDelta");

        //Act
        new PriorityQueueManager(numberOfPriorityLevels, hasPersistence, maxInMemory, queuabilityDelta);

        //Assert
        fail();
    }

    @Test
    public void shouldEnqueueFromPersistenceLayerGivenCurrentOperationNone() throws Exception {
        //Arrange
        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData =
                createDefaultEmailSchedulingDataWithPriority(assignedPriority);

        assertions.assertThat(priorityQueueManager.isCurrentOperationNone()).isTrue();

        //Act
        priorityQueueManager.enqueue(defaultEmailSchedulingData, true);
        assertions.assertThat(priorityQueueManager.isCurrentOperationEnqueuing()).isTrue();
        priorityQueueManager.completeEnqueue();

        //Assert
        assertions.assertThat(priorityQueueManager.isCurrentOperationNone()).isTrue();
        assertions.assertThat(getPriorityQueues()[assignedPriority - 1]).contains(defaultEmailSchedulingData);
    }

//    @Test
//    public void shouldNotEnqueueFromPersistenceLayerGivenCurrentOperationDequeue() throws Exception {
//        //Arrange
//        final int assignedPriority = 1;
//        final DefaultEmailSchedulingData defaultEmailSchedulingData =
//                createDefaultEmailSchedulingDataWithPriority(assignedPriority);
//
//        assertions.assertThat(priorityQueueManager.isCurrentOperationNone()).isTrue();
//        priorityQueueManager.setCurrentOperationToDequeuing();
//
//        //Act
//        priorityQueueManager.enqueue(defaultEmailSchedulingData, true);
//        assertions.assertThat(priorityQueueManager.isCurrentOperationEnqueuing()).isFalse();
//
//        //Assert
//        assertions.assertThat(priorityQueueManager.isCurrentOperationDequeuing()).isTrue();
//        assertions.assertThat(getPriorityQueues()[assignedPriority - 1]).doesNotContain(defaultEmailSchedulingData);
//    }

    private TreeSet<EmailSchedulingData>[] getPriorityQueues() {
        return (TreeSet<EmailSchedulingData>[]) ReflectionTestUtils.getField(priorityQueueManager, "queues");
    }

}