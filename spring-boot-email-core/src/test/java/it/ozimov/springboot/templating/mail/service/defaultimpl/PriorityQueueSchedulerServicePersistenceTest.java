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

import com.google.common.collect.ImmutableList;
import it.ozimov.mockito.helpers.captors.ResultCaptor;
import it.ozimov.springboot.templating.mail.BaseRedisTest;
import it.ozimov.springboot.templating.mail.model.EmailSchedulingData;
import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.templating.mail.service.EmailService;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static it.ozimov.springboot.templating.mail.service.defaultimpl.EmailSchedulingDataUtils.createDefaultEmailSchedulingDataWithPriority;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BaseRedisTest.ContextConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PriorityQueueSchedulerServicePersistenceTest extends BaseRedisTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final Timeout timeout = new Timeout(30, TimeUnit.SECONDS);

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @MockBean
    private SchedulerProperties schedulerProperties;

    @MockBean
    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    @SpyBean
    @Qualifier("defaultEmailPersistenceService")
    private DefaultPersistenceService defaultPersistenceService;

    @MockBean
    private PriorityQueueSchedulerService neverUsedSchedulerService;

    private PriorityQueueSchedulerService priorityQueueSchedulerService;

    private int priorityLevels = 5;
    private int desiredBatchSize = 10_000;
    private int minKeptInMemory = desiredBatchSize;
    private int maxKeptInMemory = Integer.MAX_VALUE;

    public ResultCaptor<Collection<EmailSchedulingData>> nextBatchResultCaptor = new ResultCaptor<>();

    public void mockSetUp() {
        doAnswer(nextBatchResultCaptor).when(defaultPersistenceService).getNextBatch(anyInt());
    }

    @Test
    public void shouldAddBatchFromPersistenceLayerWhenCreated() throws Exception {
        //Arrange
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingData3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingData1, defaultEmailSchedulingData2, defaultEmailSchedulingData3);
        defaultPersistenceService.addAll(emailSchedulingDataList);

        //Act
        createScheduler();

        //Assert
        verify(defaultPersistenceService).getNextBatch(anyInt());

        final List<Collection<EmailSchedulingData>> results = ImmutableList.copyOf(nextBatchResultCaptor.results());
        assertions.assertThat(results.get(0)).containsOnlyElementsOf(emailSchedulingDataList);
    }

    @Test
    public void shouldNotGetBatchIfMinInMemoryIsSatisfied() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        createScheduler();

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData2);
        scheduleEmailSchedulingData(defaultEmailSchedulingData1);

        TimeUnit.MILLISECONDS.sleep(30);

        //Assert
        verify(defaultPersistenceService).getNextBatch(desiredBatchSize);//On startup
    }

    @Test
    public void shouldGetBatchIfMinInMemoryIsNotSatisfied() throws Exception {
        //Arrange
        minKeptInMemory = 3;
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        createScheduler();

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData2);
        scheduleEmailSchedulingData(defaultEmailSchedulingData1);

        TimeUnit.MILLISECONDS.sleep(30);

        //Assert
        int currentlyInMemory = 1;
        InOrder inOrder = inOrder(defaultPersistenceService);
        inOrder.verify(defaultPersistenceService).getNextBatch(desiredBatchSize);//On startup
        inOrder.verify(defaultPersistenceService).getNextBatch(desiredBatchSize - currentlyInMemory);//after sending one (one buck is free now)
    }

    @Test
    public void shouldAddToPersistenceLayerWhenBeforeLastFromPersistenceLayerAndBelowMaxKeptInMemory() throws Exception {
        //Arrange
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingData3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingData1, defaultEmailSchedulingData3);
        defaultPersistenceService.addAll(emailSchedulingDataList);

        createScheduler();

        TimeUnit.MILLISECONDS.sleep(5);

        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.HOURS.toNanos(1));

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData2);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData2);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority - 1]).contains(defaultEmailSchedulingData2);

        verify(defaultPersistenceService).add(defaultEmailSchedulingData2);
    }

    @Test
    public void shouldNotAddToPersistenceLayerWhenAfterLastFromPersistenceLayerAndBelowMaxKeptInMemory() throws Exception {
        //Arrange
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingData1, defaultEmailSchedulingData2);
        defaultPersistenceService.addAll(emailSchedulingDataList);

        createScheduler();

        TimeUnit.MILLISECONDS.sleep(5);

        final DefaultEmailSchedulingData defaultEmailSchedulingData3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(3));

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData3);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData3);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority - 1]).doesNotContain(defaultEmailSchedulingData3);

        verify(defaultPersistenceService).add(defaultEmailSchedulingData3);
    }

    @Test
    public void shouldNotAddToPersistenceLayerWhenAfterLastFromPersistenceLayerAndAboveMaxKeptInMemory() throws Exception {
        //Arrange
        maxKeptInMemory = 1;
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        defaultPersistenceService.add(defaultEmailSchedulingData1);

        createScheduler();

        TimeUnit.MILLISECONDS.sleep(5);

        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(2));

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData2);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData2);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority - 1]).doesNotContain(defaultEmailSchedulingData2);

        verify(defaultPersistenceService).add(defaultEmailSchedulingData2);
    }

    @Test
    public void shouldAddToPersistenceLayerWhenBeforeLastFromPersistenceLayerAndAboveMaxKeptInMemory() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        maxKeptInMemory = 1;
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(2));

        defaultPersistenceService.add(defaultEmailSchedulingData1);

        createScheduler();

        TimeUnit.MILLISECONDS.sleep(5);

        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData2);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData2);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority - 1])
                .describedAs("Should be kicked out because of the max in memory and the fact that comes later in time").doesNotContain(defaultEmailSchedulingData1)
                .describedAs("Should be added in because of the max in memory and the fact that comes before in time").contains(defaultEmailSchedulingData2);

        verify(defaultPersistenceService).add(defaultEmailSchedulingData2);
    }

    @Test
    public void shouldAddToPersistenceLayerWhenBeforeLastFromPersistenceLayerAndAboveMaxKeptInMemoryWithEmailInOtherPriorityLevels() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        maxKeptInMemory = 3;
        mockSetUp();

        final int assignedPriority1 = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority1,
                TimeUnit.DAYS.toNanos(2));
        final int assignedPriority2 = 2;
        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority2,
                TimeUnit.DAYS.toNanos(4));
        final int assignedPriority3 = 3;
        final DefaultEmailSchedulingData defaultEmailSchedulingData3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority3,
                TimeUnit.DAYS.toNanos(3));

        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingData1, defaultEmailSchedulingData2, defaultEmailSchedulingData3);
        defaultPersistenceService.addAll(emailSchedulingDataList);

        createScheduler();

        TimeUnit.MILLISECONDS.sleep(5);

        final DefaultEmailSchedulingData defaultEmailSchedulingData4 = createDefaultEmailSchedulingDataWithPriority(assignedPriority1,
                TimeUnit.DAYS.toNanos(1));


        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData4);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData4);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority1 - 1])
                .describedAs("Should be kept in because of the max in memory and the fact that comes later in time, but not as last").contains(defaultEmailSchedulingData1)
                .describedAs("Should be added in because of the max in memory and the fact that comes before in time").contains(defaultEmailSchedulingData4);
        assertions.assertThat(queues[assignedPriority2 - 1])
                .describedAs("Should be kicked out because of the max in memory and the fact that comes as last in time").doesNotContain(defaultEmailSchedulingData2);
        assertions.assertThat(queues[assignedPriority3 - 1])
                .describedAs("Should be kept in because of the max in memory and the fact that comes later in time, but not as last").contains(defaultEmailSchedulingData3);

        verify(defaultPersistenceService).add(defaultEmailSchedulingData4);
    }

    @Test
    public void shouldAddToPersistenceLayerAndNewPriorityQueueWhenBeforeLastFromPersistenceLayerAndAboveMaxKeptInMemoryWithEmailInOtherPriorityLevels() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        maxKeptInMemory = 3;
        mockSetUp();

        final int assignedPriority1 = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority1,
                TimeUnit.DAYS.toNanos(2));
        final int assignedPriority2 = 2;
        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority2,
                TimeUnit.DAYS.toNanos(4));
        final int assignedPriority3 = 3;
        final DefaultEmailSchedulingData defaultEmailSchedulingData3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority3,
                TimeUnit.DAYS.toNanos(3));

        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingData1, defaultEmailSchedulingData2, defaultEmailSchedulingData3);
        defaultPersistenceService.addAll(emailSchedulingDataList);

        createScheduler();

        TimeUnit.MILLISECONDS.sleep(5);

        final int assignedPriority4 = 4;
        final DefaultEmailSchedulingData defaultEmailSchedulingData4 = createDefaultEmailSchedulingDataWithPriority(assignedPriority4,
                TimeUnit.DAYS.toNanos(1));


        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData4);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData4);


        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority1 - 1])
                .describedAs("Should be kept in because of the max in memory and the fact that comes later in time, but not as last").contains(defaultEmailSchedulingData1);
        assertions.assertThat(queues[assignedPriority2 - 1])
                .describedAs("Should be kicked out because of the max in memory and the fact that comes as last in time").doesNotContain(defaultEmailSchedulingData2);
        assertions.assertThat(queues[assignedPriority3 - 1])
                .describedAs("Should be kept in because of the max in memory and the fact that comes later in time, but not as last").contains(defaultEmailSchedulingData3);
        assertions.assertThat(queues[assignedPriority4 - 1])
                .describedAs("Should be added in because of the max in memory and the fact that comes before in time").contains(defaultEmailSchedulingData4);

        verify(defaultPersistenceService).add(defaultEmailSchedulingData4);
    }

    @Test
    public void shouldRemoveFromPersistenceLayerAfterSendingEmail() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        maxKeptInMemory = 3;
        mockSetUp();

        createScheduler();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.MICROSECONDS.toNanos(5));

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData);

        TimeUnit.MILLISECONDS.sleep(2 * PriorityQueueSchedulerService.CYCLE_LENGTH_IN_MILLIS);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority - 1]).isEmpty();

        verify(defaultPersistenceService).remove(defaultEmailSchedulingData.getId());
    }

    private TreeSet<EmailSchedulingData>[] getPriorityQueues() {
        return (TreeSet<EmailSchedulingData>[]) ReflectionTestUtils.getField(priorityQueueSchedulerService, "queues");
    }

    private void scheduleEmailSchedulingData(DefaultEmailSchedulingData defaultEmailSchedulingData) {
        priorityQueueSchedulerService.schedule(defaultEmailSchedulingData.getEmail(), defaultEmailSchedulingData.getScheduledDateTime(), defaultEmailSchedulingData.getAssignedPriority());
    }

    private void mockDefaultEmailSchedulingDataCreation(DefaultEmailSchedulingData defaultEmailSchedulingData) {
        doReturn(defaultEmailSchedulingData).when(priorityQueueSchedulerService)
                .buildEmailSchedulingData(defaultEmailSchedulingData.getEmail(), defaultEmailSchedulingData.getScheduledDateTime(), defaultEmailSchedulingData.getAssignedPriority(), defaultEmailSchedulingData.getAssignedPriority());
    }

    protected void createScheduler() {
        SchedulerProperties.PersistenceLayer persistenceLayer = SchedulerProperties.PersistenceLayer.builder()
                .maxKeptInMemory(maxKeptInMemory)
                .desiredBatchSize(desiredBatchSize)
                .build();
        when(schedulerProperties.getPriorityLevels()).thenReturn(priorityLevels);
        when(schedulerProperties.getPersistenceLayer()).thenReturn(persistenceLayer);

        priorityQueueSchedulerService = spy(new PriorityQueueSchedulerService(emailService, schedulerProperties, Optional.of(defaultPersistenceService)));
    }

}