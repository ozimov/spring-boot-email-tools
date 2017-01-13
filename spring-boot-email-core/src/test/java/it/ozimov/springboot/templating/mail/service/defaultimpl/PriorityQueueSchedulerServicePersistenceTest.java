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
import it.ozimov.springboot.templating.mail.CoreTestApplication;
import it.ozimov.springboot.templating.mail.model.EmailSchedulingData;
import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.templating.mail.service.EmailService;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
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
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.getSimpleMail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseRedisTest.JedisContextConfiguration.class, CoreTestApplication.class})
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PriorityQueueSchedulerServicePersistenceTest extends BaseRedisTest {

    private static final int NUM_PRIORITY_LEVELS = 10;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final Timeout timeout = new Timeout(30, TimeUnit.SECONDS);

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @MockBean
    public EmailService emailService;

    @Mock
    public MimeMessage mimeMessage;

    @SpyBean
    @Qualifier("defaultEmailPersistenceService")
    public DefaultPersistenceService defaultPersistenceService;

    public ResultCaptor<Collection<EmailSchedulingData>> nextBatchResultCaptor = new ResultCaptor<>();

    @Before
    public void setUp() {
        doAnswer(nextBatchResultCaptor).when(defaultPersistenceService).getNextBatch(anyInt());
    }

    @Test
    public void shouldAddFromPersistenceLayerWhenCreated() throws Exception {
        //Arrange
        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingData2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        final DefaultEmailSchedulingData defaultEmailSchedulingData3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority);

        defaultPersistenceService.add(defaultEmailSchedulingData1);
        defaultPersistenceService.add(defaultEmailSchedulingData2);
        defaultPersistenceService.add(defaultEmailSchedulingData3);

        //Act
        PriorityQueueSchedulerService priorityQueueSchedulerService = scheduler();

        //Assert
        verify(defaultPersistenceService, atLeastOnce()).getNextBatch(anyInt());

        List<Collection<EmailSchedulingData>> results = ImmutableList.copyOf(nextBatchResultCaptor.results());
        assertions.assertThat(results.get(0));

    }

    protected PriorityQueueSchedulerService scheduler() {
        final PriorityQueueSchedulerService schedulerService = spy(new PriorityQueueSchedulerService(emailService, NUM_PRIORITY_LEVELS, Optional.of(defaultPersistenceService)));
        return schedulerService;
    }

}