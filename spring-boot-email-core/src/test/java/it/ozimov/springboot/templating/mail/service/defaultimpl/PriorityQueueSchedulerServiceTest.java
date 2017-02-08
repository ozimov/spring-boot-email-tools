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

import it.ozimov.springboot.templating.mail.ContextBasedTest;
import it.ozimov.springboot.templating.mail.model.Email;
import it.ozimov.springboot.templating.mail.service.EmailService;
import it.ozimov.springboot.templating.mail.service.ServiceStatus;
import it.ozimov.springboot.templating.mail.utils.TimeUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.getSimpleMail;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class PriorityQueueSchedulerServiceTest implements ContextBasedTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final Timeout timeout = new Timeout(60, SECONDS);

    @MockBean
    private EmailService emailService;

    @MockBean
    private SchedulerProperties schedulerProperties;

    @Mock
    private MimeMessage mimeMessage;

    private static long oneSecond() {
        return SECONDS.toSeconds(1);
    }

    private static long twoSeconds() {
        return SECONDS.toSeconds(2);
    }

    private static long threeSeconds() {
        return SECONDS.toSeconds(3);
    }

    private static long oneSecondInMillis() {
        return SECONDS.toMillis(1);
    }

    private static long fiveSecondsInMillis() {
        return SECONDS.toMillis(5);
    }

    private static Email lowPriority() throws UnsupportedEncodingException {
        return getSimpleMail(new InternetAddress("virgilio@marone.roma", "Publio Virgilio Marone"));
    }

    private static Email midPriority() throws UnsupportedEncodingException {
        return getSimpleMail(new InternetAddress("lucio.domizio.enobarbo@hotmail.roma", "Nerone"));
    }

    private static Email highPriority() throws UnsupportedEncodingException {
        return getSimpleMail(new InternetAddress("cicero@mala-tempora.currunt", "Marco Tullio Cicerone"));
    }

    @Before
    public void setUp() {
        when(schedulerProperties.getPriorityLevels()).thenReturn(1);
        when(schedulerProperties.getPersistenceLayer()).thenReturn(
                SchedulerProperties.PersistenceLayer.builder().desiredBatchSize(0).maxKeptInMemory(Integer.MAX_VALUE).build());
    }

    @Test
    public void testAnEmailIsScheduledAndSent() throws Exception {
        //Arrange
        final PriorityQueueSchedulerService priorityQueueSchedulerService = scheduler(1);
        final Email email = getSimpleMail();

        //Act
        priorityQueueSchedulerService.schedule(email, TimeUtils.offsetDateTimeNow(), 1);
        Thread.sleep(oneSecondInMillis());

        //Assert
        verify(emailService).send(email);
    }

    @Test
    public void testAnEmailWithTemplateIsScheduledAndSent() throws Exception {
        //Arrange
        final PriorityQueueSchedulerService priorityQueueSchedulerService = scheduler(1);
        final Email email = getSimpleMail();

        //Act
        priorityQueueSchedulerService.schedule(email, TimeUtils.offsetDateTimeNow(), 1,
                TemplatingTestUtils.TEMPLATE, TemplatingTestUtils.MODEL_OBJECT);
        Thread.sleep(oneSecondInMillis());

        //Assert
        verify(emailService).send(email,
                TemplatingTestUtils.TEMPLATE,
                TemplatingTestUtils.MODEL_OBJECT);
    }

    @Test
    public void testPriorityIsRespectedForSameDateTime() throws Exception {
        //Arrange
        final PriorityQueueSchedulerService priorityQueueSchedulerService = scheduler(3);
        final Email emailLowPriority = lowPriority();
        final Email emailMidPriority = midPriority();
        final Email emailHighPriority = highPriority();

        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();

        //Act
        priorityQueueSchedulerService.schedule(emailLowPriority, dateTime.plusSeconds(twoSeconds()), 3);
        priorityQueueSchedulerService.schedule(emailHighPriority, dateTime.plusSeconds(twoSeconds()), 1);
        priorityQueueSchedulerService.schedule(emailMidPriority, dateTime.plusSeconds(twoSeconds()), 2);
        Thread.sleep(fiveSecondsInMillis());

        //Assert
        final InOrder inOrder = inOrder(emailService);
        inOrder.verify(emailService).send(emailHighPriority);
        inOrder.verify(emailService).send(emailMidPriority);
        inOrder.verify(emailService).send(emailLowPriority);
    }

    @Test
    public void testPriorityIsRespectedDifferentDateTime() throws Exception {
        //Arrange
        final PriorityQueueSchedulerService priorityQueueSchedulerService = scheduler(3);
        final Email emailLowPriority = lowPriority();
        final Email emailMidPriority = midPriority();
        final Email emailHighPriority = highPriority();

        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();

        //Act
        priorityQueueSchedulerService.schedule(emailHighPriority, dateTime.plusSeconds(threeSeconds()), 1);
        priorityQueueSchedulerService.schedule(emailMidPriority, dateTime.plusSeconds(twoSeconds()), 2);
        priorityQueueSchedulerService.schedule(emailLowPriority, dateTime.plusSeconds(oneSecond()), 3);
        Thread.sleep(fiveSecondsInMillis());

        //Assert
        final InOrder inOrder = inOrder(emailService);
        inOrder.verify(emailService).send(emailLowPriority);
        inOrder.verify(emailService).send(emailMidPriority);
        inOrder.verify(emailService).send(emailHighPriority);
    }

    @Test
    public void testCanClose() throws Exception {
        //Arrange
        PriorityQueueSchedulerService priorityQueueSchedulerService = scheduler(1);

        //Act
        Thread.sleep(oneSecondInMillis());//Consumer can starts
        priorityQueueSchedulerService.cleanUp();
        Thread.sleep(oneSecondInMillis());

        //Assert
        given(priorityQueueSchedulerService.status()).assertThat(CoreMatchers.is(ServiceStatus.CLOSED));
    }

    private PriorityQueueSchedulerService scheduler(int numPriorityLevels) {
        when(schedulerProperties.getPriorityLevels()).thenReturn(numPriorityLevels);

        final PriorityQueueSchedulerService schedulerService = new PriorityQueueSchedulerService(emailService,
                schedulerProperties,
                Optional.empty());
        return schedulerService;
    }

}