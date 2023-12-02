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

import it.ozimov.mockito.helpers.captors.ResultCaptor;
import it.ozimov.springboot.mail.ContextBasedTest;
import it.ozimov.springboot.mail.configuration.EmailSchedulerProperties;
import it.ozimov.springboot.mail.logging.EmailLogRenderer;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.service.EmailService;
import it.ozimov.springboot.mail.service.ServiceStatus;
import it.ozimov.springboot.mail.utils.TimeUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static it.ozimov.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class PriorityQueueEmailSchedulerServiceTest implements ContextBasedTest {

    @Rule
    public final Timeout timeout = new Timeout(30, SECONDS);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @MockBean
    private EmailService emailService;

    @MockBean
    private EmailSchedulerProperties emailSchedulerProperties;

    @MockBean
    private EmailLogRenderer emailLogRenderer;

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

    private static long twoSecondsInMillis() {
        return SECONDS.toMillis(2);
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
        when(emailSchedulerProperties.getPriorityLevels()).thenReturn(1);
        when(emailSchedulerProperties.getPersistence()).thenReturn(
                EmailSchedulerProperties.Persistence.builder().desiredBatchSize(0).maxKeptInMemory(Integer.MAX_VALUE).build());
    }

    @Test
    public void shouldScheduleAndSendEmailWithoutTemplateAndOffsetDateTime() throws Exception {
        //Arrange
        final PriorityQueueEmailSchedulerService priorityQueueSchedulerService = scheduler(1);
        final Email email = getSimpleMail();

        //Act
        priorityQueueSchedulerService.schedule(email, 1);
        Thread.sleep(twoSecondsInMillis());

        //Assert
        verify(emailService).send(email);
    }

    @Test
    public void shouldScheduleAndSendEmailWithoutTemplate() throws Exception {
        //Arrange
        final PriorityQueueEmailSchedulerService priorityQueueSchedulerService = scheduler(1);
        final Email email = getSimpleMail();

        //Act
        priorityQueueSchedulerService.schedule(email, TimeUtils.offsetDateTimeNow(), 1);
        Thread.sleep(twoSecondsInMillis());

        //Assert
        verify(emailService).send(email);
    }

    @Test
    public void shouldScheduleAndSendEmailWithTemplateAndOffsetDateTime() throws Exception {
        //Arrange
        final PriorityQueueEmailSchedulerService priorityQueueSchedulerService = scheduler(1);
        final Email email = getSimpleMail();

        //Act
        priorityQueueSchedulerService.schedule(email,1, TemplatingTestUtils.TEMPLATE,
                TemplatingTestUtils.MODEL_OBJECT);
        Thread.sleep(twoSecondsInMillis());

        //Assert
        verify(emailService).send(email,
                TemplatingTestUtils.TEMPLATE,
                TemplatingTestUtils.MODEL_OBJECT);
    }

    @Test
    public void shouldScheduleAndSendEmailWithTemplate() throws Exception {
        //Arrange
        final PriorityQueueEmailSchedulerService priorityQueueSchedulerService = scheduler(1);
        final Email email = getSimpleMail();

        //Act
        priorityQueueSchedulerService.schedule(email, TimeUtils.offsetDateTimeNow(), 1,
                TemplatingTestUtils.TEMPLATE, TemplatingTestUtils.MODEL_OBJECT);
        Thread.sleep(twoSecondsInMillis());

        //Assert
        verify(emailService).send(email,
                TemplatingTestUtils.TEMPLATE,
                TemplatingTestUtils.MODEL_OBJECT);
    }

    @Test
    public void shouldRespectPriorityForSameDateTime() throws Exception {
        //Arrange
        final PriorityQueueEmailSchedulerService priorityQueueSchedulerService = scheduler(3);
        final Email emailLowPriority = lowPriority();
        final Email emailMidPriority = midPriority();
        final Email emailHighPriority = highPriority();

        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();

        //Act
        priorityQueueSchedulerService.schedule(emailLowPriority, dateTime.plusSeconds(twoSeconds()), 3);
        priorityQueueSchedulerService.schedule(emailHighPriority, dateTime.plusSeconds(twoSeconds()), 1);
        priorityQueueSchedulerService.schedule(emailMidPriority, dateTime.plusSeconds(twoSeconds()), 2);

        TimeUnit.SECONDS.sleep(5);

        //Assert
        final InOrder inOrder = inOrder(emailService);
        inOrder.verify(emailService).send(emailHighPriority);
        inOrder.verify(emailService).send(emailMidPriority);
        inOrder.verify(emailService).send(emailLowPriority);
    }

    @Test
    public void shouldRespectPriorityWhenDifferentDateTime() throws Exception {
        //Arrange
        final PriorityQueueEmailSchedulerService priorityQueueSchedulerService = scheduler(3);
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
    public void shouldNormalizePriority() throws Exception {
        //Arrange
        final int maxPriorityLevels = 1;
        final int nonAvailablePriorityLevel = 2;
        assertions.assertThat(nonAvailablePriorityLevel).isGreaterThan(maxPriorityLevels);

        final PriorityQueueEmailSchedulerService priorityQueueSchedulerService = scheduler(maxPriorityLevels);
        final Email email = getSimpleMail(new InternetAddress("virgilio@marone.roma", "Publio Virgilio Marone"));

        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();

        ResultCaptor<Integer> normalizedPriorityLevelCaptor = new ResultCaptor<>();
        doAnswer(normalizedPriorityLevelCaptor).when(priorityQueueSchedulerService).normalizePriority(nonAvailablePriorityLevel);


        //Act
        priorityQueueSchedulerService.schedule(email, dateTime.plusSeconds(threeSeconds()), nonAvailablePriorityLevel);

        //Assert
        verify(priorityQueueSchedulerService).normalizePriority(nonAvailablePriorityLevel);
        assertions.assertThat(normalizedPriorityLevelCaptor.result()).isEqualTo(maxPriorityLevels);
    }

    @Test
    public void shouldClose() throws Exception {
        //Arrange
        PriorityQueueEmailSchedulerService priorityQueueSchedulerService = scheduler(1);

        //Act
        priorityQueueSchedulerService.cleanUp();
        Thread.sleep(twoSecondsInMillis());

        //Assert
        given(priorityQueueSchedulerService.status()).assertThat(CoreMatchers.is(ServiceStatus.CLOSED));
    }

    private PriorityQueueEmailSchedulerService scheduler(int numPriorityLevels) throws InterruptedException {
        when(emailSchedulerProperties.getPriorityLevels()).thenReturn(numPriorityLevels);
        when(emailLogRenderer.registerLogger(any(Logger.class))).thenReturn(emailLogRenderer);

        final PriorityQueueEmailSchedulerService schedulerService = spy(new PriorityQueueEmailSchedulerService(emailService,
                emailSchedulerProperties,
                Optional.empty(),
                emailLogRenderer));
        return schedulerService;
    }

}