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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static it.ozimov.springboot.templating.mail.service.defaultimpl.EmailSchedulingDataUtils.createDefaultEmailSchedulingDataWithPriority;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BaseRedisTest.ContextConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(properties = {"", "", ""})
public class PriorityQueueSchedulerServicePersistenceTest extends BaseRedisTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

//    @Rule
//    public final Timeout timeout = new Timeout(30, TimeUnit.SECONDS);

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

    private int priorityLevels = 5;
    private int desiredBatchSize = 100_000;
    private int maxKeptInMemory = Integer.MAX_VALUE;

    public ResultCaptor<Collection<EmailSchedulingData>> nextBatchResultCaptor = new ResultCaptor<>();

    @Before
    public void setUp() {
        doAnswer(nextBatchResultCaptor).when(defaultPersistenceService).getNextBatch(anyInt());
        when(schedulerProperties.getPriorityLevels()).thenReturn(priorityLevels);
        when(schedulerProperties.getPersistenceLayer()).thenReturn(SchedulerProperties.PersistenceLayer.builder()
                .desiredBatchSize(1)
                .maxKeptInMemory(1)
                .build());
    }

//    @Test
//    public void shouldAddBatchFromPersistenceLayerWhenCreated() throws Exception {
//        //Arrange
//        final int assignedPriority = 1;
//        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
//        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
//        final DefaultEmailSchedulingData defaultEmailSchedulingData3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
//
//        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingData1, defaultEmailSchedulingData2, defaultEmailSchedulingData3);
//        defaultPersistenceService.addAll(emailSchedulingDataList);
//
//        //Act
//        scheduler();
//
//        //Assert
//        verify(defaultPersistenceService, atLeastOnce()).getNextBatch(anyInt());
//
//        final List<Collection<EmailSchedulingData>> results = ImmutableList.copyOf(nextBatchResultCaptor.results());
//
//        assertions.assertThat(results.get(0)).containsOnlyElementsOf(emailSchedulingDataList);
//    }

    @Test
    public void shouldAddFromPersistenceLayerWhenBeforeLastFromPersistenceLayerAndBelowMaxKeptInMemory() throws Exception {
        //Arrange
        priorityLevels = 5;
        desiredBatchSize = 10_000;
        maxKeptInMemory = Integer.MAX_VALUE;


        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority,
                TimeUnit.DAYS.toNanos(1));

        List<EmailSchedulingData> emailSchedulingDataList = ImmutableList.of(defaultEmailSchedulingData1, defaultEmailSchedulingData2);
        defaultPersistenceService.addAll(emailSchedulingDataList);
        PriorityQueueSchedulerService priorityQueueSchedulerService = scheduler();
        TimeUnit.MILLISECONDS.sleep(5);
        //Act
        //  priorityQueueSchedulerService.schedule();

        //Assert
        verify(defaultPersistenceService, atLeastOnce()).getNextBatch(anyInt());

        final List<Collection<EmailSchedulingData>> results = ImmutableList.copyOf(nextBatchResultCaptor.results());

        assertions.assertThat(results.get(0)).containsOnlyElementsOf(emailSchedulingDataList);
//        assertions.assertThat(results.get(1)).containsOnly(defaultEmailSchedulingData3);
//        IntStream.range(1, results.size())
//                .forEach(i ->
////                                System.out.println(results.get(i))
//                        assertions.assertThat(results.get(i)).isEmpty()
//                );
    }

    protected PriorityQueueSchedulerService scheduler() {
        SchedulerProperties.PersistenceLayer persistenceLayer = SchedulerProperties.PersistenceLayer.builder()
                .maxKeptInMemory(maxKeptInMemory)
                .desiredBatchSize(desiredBatchSize)
                .build();
        when(schedulerProperties.getPriorityLevels()).thenReturn(priorityLevels);
        when(schedulerProperties.getPersistenceLayer()).thenReturn(persistenceLayer);

        final PriorityQueueSchedulerService schedulerService = spy(new PriorityQueueSchedulerService(emailService, schedulerProperties, Optional.of(defaultPersistenceService)));
        return schedulerService;
    }

}