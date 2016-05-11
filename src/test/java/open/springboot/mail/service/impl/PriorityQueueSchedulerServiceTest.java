package open.springboot.mail.service.impl;

import open.springboot.mail.model.Email;
import open.springboot.mail.service.EmailService;
import open.springboot.mail.utils.TimeUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import testutils.TestApplication;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static open.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
@WebIntegrationTest("server.port=0")
public class PriorityQueueSchedulerServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public Timeout globalTimeout= new Timeout(10, TimeUnit.SECONDS);

    @Mock
    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    @Test
    public void testAnEmailIsScheduled() throws Exception {
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
    public void testPriorityIsRespected() throws Exception {
        //Arrange
        final PriorityQueueSchedulerService priorityQueueSchedulerService = scheduler(2);
        final Email emailLowPriority = getSimpleMail(new InternetAddress("virgilio@marone.roma", "Publio Virgilio Marone"));
        final Email emailHighPriority = getSimpleMail(new InternetAddress("cicero@mala-tempora.currunt", "Marco Tullio Cicerone"));

        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();

        //Act
        priorityQueueSchedulerService.schedule(emailLowPriority, dateTime.plusSeconds(twoSeconds()), 2);
        priorityQueueSchedulerService.schedule(emailHighPriority, dateTime.plusSeconds(twoSeconds()), 1);
        Thread.sleep(fiveSecondsInMillis());

        //Assert
        final InOrder inOrder = inOrder(emailService);
        inOrder.verify(emailService).send(emailHighPriority);
        inOrder.verify(emailService).send(emailLowPriority);
    }

    private PriorityQueueSchedulerService scheduler(int numPriorityLevels){
        return new PriorityQueueSchedulerService(emailService, numPriorityLevels);
    }

    private static long twoSeconds() {
        return SECONDS.toSeconds(2);
    }

    private static long oneSecondInMillis() {
        return SECONDS.toMillis(1);
    }

    private static long twoSecondsInMillis() {
        return twoSeconds();
    }

    private static long threeSecondsInMillis() {
        return SECONDS.toMillis(3);
    }

    private static long fiveSecondsInMillis() {
        return SECONDS.toMillis(5);
    }
}