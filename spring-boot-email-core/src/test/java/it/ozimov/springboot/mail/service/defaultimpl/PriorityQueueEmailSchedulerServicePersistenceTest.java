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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.ozimov.mockito.helpers.captors.ResultCaptor;
import it.ozimov.springboot.mail.BaseRedisTest;
import it.ozimov.springboot.mail.configuration.EmailSchedulerProperties;
import it.ozimov.springboot.mail.logging.EmailLogRenderer;
import it.ozimov.springboot.mail.model.EmailSchedulingData;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.mail.service.EmailService;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static it.ozimov.springboot.mail.service.defaultimpl.EmailSchedulingDataUtils.createDefaultEmailSchedulingDataWithPriority;
import static it.ozimov.springboot.mail.service.defaultimpl.PriorityQueueEmailSchedulerService.CONSUMER_CYCLE_LENGTH;
import static it.ozimov.springboot.mail.service.defaultimpl.PriorityQueueEmailSchedulerService.RESUMER_CYCLE_LENGTH;
import static java.lang.Math.max;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BaseRedisTest.ContextConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PriorityQueueEmailSchedulerServicePersistenceTest extends BaseRedisTest {

    private static final long HALF_RESUMER_CYCLE_LENGTH_IN_MILLIS = (long) Math.ceil(RESUMER_CYCLE_LENGTH.toMillis() / 2.0);
    private static final long RESUMER_CYCLE_LENGTH_PLUS_HALF_IN_MILLIS = RESUMER_CYCLE_LENGTH.toMillis() + HALF_RESUMER_CYCLE_LENGTH_IN_MILLIS;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final Timeout timeout =
            new Timeout(max(30000L, max(RESUMER_CYCLE_LENGTH.getSeconds(), CONSUMER_CYCLE_LENGTH.getSeconds())), SECONDS);

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @MockBean
    private EmailSchedulerProperties emailSchedulerProperties;

    @MockBean
    private EmailService emailService;

    @MockBean
    private EmailLogRenderer emailLogRenderer;

    @Mock
    private MimeMessage mimeMessage;

    @SpyBean
    @Qualifier("defaultEmailPersistenceService")
    private DefaultPersistenceService defaultPersistenceService;

    @MockBean
    private PriorityQueueEmailSchedulerService neverUsedSchedulerService;

    private PriorityQueueEmailSchedulerService priorityQueueSchedulerService;

    private int priorityLevels = 5;
    private int desiredBatchSize = 10_000;
    private int minKeptInMemory = desiredBatchSize;
    private int maxKeptInMemory = Integer.MAX_VALUE;

    public ResultCaptor<Collection<EmailSchedulingData>> nextBatchResultCaptor;

    public void mockSetUp() {
        nextBatchResultCaptor = new ResultCaptor<>();
        doAnswer(nextBatchResultCaptor).when(defaultPersistenceService).getNextBatch(anyInt());
    }

    @Test
    public void shouldAddBatchFromPersistenceWhenCreated() throws Exception {
        //Arrange
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingData3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority, TimeUnit.DAYS.toNanos(1));
        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingData1, defaultEmailSchedulingData2, defaultEmailSchedulingData3);
        defaultPersistenceService.addAll(emailSchedulingDataList);

        //Act
        createScheduler();
        SECONDS.sleep(1);

        //Assert
        verify(defaultPersistenceService).getNextBatch(anyInt()); //Resumer at startup tries to load and then wait

        final List<Collection<EmailSchedulingData>> results = ImmutableList.copyOf(nextBatchResultCaptor.results());
        assertions.assertThat(results.get(0)).containsOnlyElementsOf(emailSchedulingDataList);
    }

    @Test
    public void shouldNotGetBatchIfMinInMemoryIsSatisfied() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        maxKeptInMemory = 10_000;
        desiredBatchSize = 1_000;
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData = createDefaultEmailSchedulingDataWithPriority(assignedPriority, TimeUnit.DAYS.toNanos(1));

        createScheduler();

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData);
        MILLISECONDS.sleep(HALF_RESUMER_CYCLE_LENGTH_IN_MILLIS);

        //Assert
        verify(defaultPersistenceService).getNextBatch(desiredBatchSize); //Just on load

        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority - 1]).containsOnly(defaultEmailSchedulingData);
    }

    @Test
    public void shouldTryToGetBatchIfMinInMemoryIsNotSatisfied() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        maxKeptInMemory = 10_000;
        desiredBatchSize = 1_000;
        mockSetUp();

        createScheduler();

        //Act

        MILLISECONDS.sleep(RESUMER_CYCLE_LENGTH_PLUS_HALF_IN_MILLIS);

        //Assert
        verify(defaultPersistenceService, times(2)).getNextBatch(desiredBatchSize); //On load and after
    }

    @Test
    public void shouldAddToQueueWhenBeforeLastFromPersistence() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        maxKeptInMemory = 2;
        desiredBatchSize = 2;
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.MINUTES.toNanos(10));
        final DefaultEmailSchedulingData defaultEmailSchedulingData3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingData1, defaultEmailSchedulingData3);
        defaultPersistenceService.addAll(emailSchedulingDataList);

        createScheduler();

        SECONDS.sleep(1);

        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.HOURS.toNanos(1));

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData2);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData2);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority - 1]).as("Is the first to fire so must be in the queue").contains(defaultEmailSchedulingData1)
                .as("It was scheduled after the start, but it must fire before the second loaded").contains(defaultEmailSchedulingData2)
                .as("It was in the queue, but should be kicked out by the first scheduled in this run").doesNotContain(defaultEmailSchedulingData3);

        verify(defaultPersistenceService).add(defaultEmailSchedulingData2);
    }

    @Test
    public void shouldNotAddToQueueWhenAfterLastFromPersistenceAndBelowMaxKeptInMemory() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        maxKeptInMemory = 2;
        desiredBatchSize = 2;
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.MINUTES.toNanos(10));
        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.HOURS.toNanos(1));

        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingData1, defaultEmailSchedulingData2);
        defaultPersistenceService.addAll(emailSchedulingDataList);

        createScheduler();

        SECONDS.sleep(1);

        final DefaultEmailSchedulingData defaultEmailSchedulingData3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData3);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData3);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority - 1]).contains(defaultEmailSchedulingData1)
                .contains(defaultEmailSchedulingData2)
                .as("It was scheduled after the start and is after the last loaded form persistence layer").doesNotContain(defaultEmailSchedulingData3);

        verify(defaultPersistenceService).add(defaultEmailSchedulingData3);
    }

    @Test
    public void shouldNotAddToQueueWhenAfterLastFromPersistenceAndAboveMaxKeptInMemory() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        desiredBatchSize = 1;
        maxKeptInMemory = 1;
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        defaultPersistenceService.add(defaultEmailSchedulingData1);

        createScheduler();

        MILLISECONDS.sleep(5);

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
    public void shouldAddToQueueWhenBeforeLastFromPersistenceAndAboveMaxKeptInMemory() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        desiredBatchSize = 1;
        maxKeptInMemory = 1;
        mockSetUp();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(2));

        defaultPersistenceService.add(defaultEmailSchedulingData1);

        createScheduler();

        SECONDS.sleep(2);

        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData2);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData2);

        SECONDS.sleep(RESUMER_CYCLE_LENGTH.getSeconds() + 1);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority - 1])
                .describedAs("Should be kicked out because of the max in memory and the fact that comes later in time").doesNotContain(defaultEmailSchedulingData1)
                .describedAs("Should be added in because of the max in memory and the fact that comes before in time").contains(defaultEmailSchedulingData2);

        verify(defaultPersistenceService).add(defaultEmailSchedulingData2);
    }

    @Test
    public void shouldAddToQueueWhenBeforeLastFromPersistenceAndAboveMaxKeptInMemoryWithEmailInOtherPriorityLevels() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        desiredBatchSize = 3;
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

        SECONDS.sleep(2);

        final DefaultEmailSchedulingData defaultEmailSchedulingData4 = createDefaultEmailSchedulingDataWithPriority(assignedPriority1,
                TimeUnit.DAYS.toNanos(1));

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData4);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData4);

        SECONDS.sleep(RESUMER_CYCLE_LENGTH.getSeconds() + 1);

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
    public void shouldAddToQueueAndNewPriorityQueueWhenBeforeLastFromPersistenceAndAboveMaxKeptInMemoryWithEmailInOtherPriorityLevels() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        desiredBatchSize = 3;
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

        SECONDS.sleep(1);

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
    public void shouldRemoveFromPersistenceAfterSendingEmail() throws Exception {
        //Arrange
        minKeptInMemory = 1;
        desiredBatchSize = 3;
        maxKeptInMemory = 3;
        mockSetUp();

        createScheduler();

        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.MICROSECONDS.toNanos(5));

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingData);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingData);

        SECONDS.sleep(2 * CONSUMER_CYCLE_LENGTH.getSeconds());

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedPriority - 1]).isEmpty();

        verify(defaultPersistenceService).remove(defaultEmailSchedulingData.getId());
    }

    @Test
    public void shouldHaveTheExpectedNumberOfEmailsInMemoryWhenPersistenceIsEmpty() throws Exception {
        //Arrange
        priorityLevels = 3;

        desiredBatchSize = 4;
        minKeptInMemory = 3;
        maxKeptInMemory = 4;
        mockSetUp();

        final int assignedHighPriority = 1;
        final int assignedMidPriority = 2;
        final int assignedLowPriority = 3;
        final DefaultEmailSchedulingData defaultEmailSchedulingDataHigh1 = createDefaultEmailSchedulingDataWithPriority(assignedHighPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingDataHigh2 = createDefaultEmailSchedulingDataWithPriority(assignedHighPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingDataHigh3 = createDefaultEmailSchedulingDataWithPriority(assignedHighPriority);

        final DefaultEmailSchedulingData defaultEmailSchedulingDataMid1 = createDefaultEmailSchedulingDataWithPriority(assignedMidPriority, TimeUnit.HOURS.toNanos(6));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataMid2 = createDefaultEmailSchedulingDataWithPriority(assignedMidPriority, TimeUnit.HOURS.toNanos(6));

        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow1 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow2 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow3 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow4 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow5 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow6 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow7 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow8 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));

        createScheduler();


        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataHigh1);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataHigh2);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataHigh3);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataMid1);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataMid2);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataLow1);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataLow2);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataLow3);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataLow4);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataLow5);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataLow6);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataLow7);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataLow8);

        //Act
        scheduleEmailSchedulingData(defaultEmailSchedulingDataHigh1);//1) -- Should fire asap
        scheduleEmailSchedulingData(defaultEmailSchedulingDataHigh2);//2) -- Should fire asap
        scheduleEmailSchedulingData(defaultEmailSchedulingDataHigh3);//3) -- Should fire asap
        SECONDS.sleep(1);//Just to be sure that those that can be sent immediately are sent

        scheduleEmailSchedulingData(defaultEmailSchedulingDataMid1);//4) --
        scheduleEmailSchedulingData(defaultEmailSchedulingDataMid2);//5) -- this could be not in memory

        scheduleEmailSchedulingData(defaultEmailSchedulingDataLow1);//6) -- this could be not in memory
        scheduleEmailSchedulingData(defaultEmailSchedulingDataLow2);//7) -- this could be not in memory
        scheduleEmailSchedulingData(defaultEmailSchedulingDataLow3);//8) -- this MUST NOT be in memory
        scheduleEmailSchedulingData(defaultEmailSchedulingDataLow4);//9) -- this MUST NOT be in memory
        scheduleEmailSchedulingData(defaultEmailSchedulingDataLow5);//10) -- this MUST NOT be in memory
        scheduleEmailSchedulingData(defaultEmailSchedulingDataLow6);//11) -- this MUST NOT be in memory
        scheduleEmailSchedulingData(defaultEmailSchedulingDataLow7);//12) -- this MUST NOT be in memory
        scheduleEmailSchedulingData(defaultEmailSchedulingDataLow8);//13) -- this MUST NOT be in memory

        MILLISECONDS.sleep(RESUMER_CYCLE_LENGTH_PLUS_HALF_IN_MILLIS);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedHighPriority - 1]).as("High priority emails queue should be empty").isEmpty();
        assertions.assertThat(queues[assignedMidPriority - 1]).as("Mid priority emails queue should have two emails").hasSize(2);
        assertions.assertThat(queues[assignedLowPriority - 1]).as("Low priority emails queue should have two emails").hasSize(2);
    }

    @Test
    public void shouldHaveTheExpectedNumberOfEmailsInMemoryWhenPersistenceIsNotEmpty() throws Exception {
        //Arrange
        priorityLevels = 3;

        desiredBatchSize = 4;
        minKeptInMemory = 3;
        maxKeptInMemory = 7;
        mockSetUp();

        final int assignedHighPriority = 1;
        final int assignedMidPriority = 2;
        final int assignedLowPriority = 3;
        final DefaultEmailSchedulingData defaultEmailSchedulingDataHigh1 = createDefaultEmailSchedulingDataWithPriority(assignedHighPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingDataHigh2 = createDefaultEmailSchedulingDataWithPriority(assignedHighPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingDataHigh3 = createDefaultEmailSchedulingDataWithPriority(assignedHighPriority);

        final DefaultEmailSchedulingData defaultEmailSchedulingDataMid1 = createDefaultEmailSchedulingDataWithPriority(assignedMidPriority, TimeUnit.HOURS.toNanos(6));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataMid2 = createDefaultEmailSchedulingDataWithPriority(assignedMidPriority, TimeUnit.HOURS.toNanos(6));

        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow1 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow2 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow3 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));
        final DefaultEmailSchedulingData defaultEmailSchedulingDataLow4 = createDefaultEmailSchedulingDataWithPriority(assignedLowPriority, TimeUnit.DAYS.toNanos(1));

        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingDataLow1, defaultEmailSchedulingDataLow2, defaultEmailSchedulingDataLow3, defaultEmailSchedulingDataLow4);
        defaultPersistenceService.addAll(emailSchedulingDataList);

        createScheduler();
        //Now the resumer puts in memory the 4 low priority emails

        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataHigh1);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataHigh2);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataHigh3);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataMid1);
        mockDefaultEmailSchedulingDataCreation(defaultEmailSchedulingDataMid2);

        //Act
        MILLISECONDS.sleep(50);
        //Now the 4 low priority emails should have been loaded

        //Should have 4 low priority in memory
        scheduleEmailSchedulingData(defaultEmailSchedulingDataMid1);  //1) -- Mid priority, so is put in memory and one from the low priority sent out
        scheduleEmailSchedulingData(defaultEmailSchedulingDataMid2);  //2) -- Mid priority, so is put in memory and one from the low priority sent out
        //the two mid priority should send out
        scheduleEmailSchedulingData(defaultEmailSchedulingDataHigh1); //3) -- Should fire asap so is put in memory and one from the low priority sent out
        scheduleEmailSchedulingData(defaultEmailSchedulingDataHigh2); //4) -- Should fire asap so is put in memory and one from the low priority sent out
        scheduleEmailSchedulingData(defaultEmailSchedulingDataHigh3); //5) -- Should fire asap so is put in memory and one from the low priority sent out

        //The 4 emails with higher priority have been loaded

        MILLISECONDS.sleep(RESUMER_CYCLE_LENGTH_PLUS_HALF_IN_MILLIS);

        //Assert
        TreeSet<EmailSchedulingData>[] queues = getPriorityQueues();
        assertions.assertThat(queues[assignedHighPriority - 1]).as("High priority emails queue should be empty").isEmpty();
        assertions.assertThat(queues[assignedMidPriority - 1]).as("Mid priority emails queue should have two emails").hasSize(2);
        assertions.assertThat(queues[assignedLowPriority - 1]).as("Low priority emails queue should have between three and four emails")
                .matches(set -> Iterables.size(set) == 3 || Iterables.size(set) == 4);
    }

    private TreeSet<EmailSchedulingData>[] getPriorityQueues() {
        PriorityQueueManager priorityQueueManager = (PriorityQueueManager) ReflectionTestUtils.getField(priorityQueueSchedulerService, "priorityQueueManager");
        return (TreeSet<EmailSchedulingData>[]) ReflectionTestUtils.getField(priorityQueueManager, "queues");
    }

    private void scheduleEmailSchedulingData(DefaultEmailSchedulingData defaultEmailSchedulingData) {
        priorityQueueSchedulerService.schedule(defaultEmailSchedulingData.getEmail(), defaultEmailSchedulingData.getScheduledDateTime(), defaultEmailSchedulingData.getAssignedPriority());
    }

    private void mockDefaultEmailSchedulingDataCreation(DefaultEmailSchedulingData defaultEmailSchedulingData) {
        doReturn(defaultEmailSchedulingData).when(priorityQueueSchedulerService)
                .buildEmailSchedulingData(defaultEmailSchedulingData.getEmail(), defaultEmailSchedulingData.getScheduledDateTime(), defaultEmailSchedulingData.getAssignedPriority(), defaultEmailSchedulingData.getAssignedPriority());
    }

    protected void createScheduler() throws InterruptedException {
        //Sometimes Spring fails in creating the mock for the bean. Bad Spring! Bad!
        emailSchedulerProperties = mock(EmailSchedulerProperties.class);
        EmailSchedulerProperties.Persistence persistence = EmailSchedulerProperties.Persistence.builder()
                .minKeptInMemory(minKeptInMemory)
                .maxKeptInMemory(maxKeptInMemory)
                .desiredBatchSize(desiredBatchSize)
                .build();

        when(emailSchedulerProperties.getPriorityLevels()).thenReturn(priorityLevels);
        when(emailSchedulerProperties.getPersistence()).thenReturn(persistence);
        EmailSchedulerProperties.checkIsValid(emailSchedulerProperties);
        when(emailLogRenderer.registerLogger(any(Logger.class))).thenReturn(emailLogRenderer);
        priorityQueueSchedulerService = spy(new PriorityQueueEmailSchedulerService(emailService, emailSchedulerProperties, Optional.of(defaultPersistenceService), emailLogRenderer));
    }

}