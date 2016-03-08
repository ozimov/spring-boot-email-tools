package open.springboot.mail.service.impl;

import com.google.common.collect.ImmutableMap;
import open.springboot.mail.model.Email;
import open.springboot.mail.service.EmailService;
import open.springboot.mail.service.TemplateService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import testutils.TestApplication;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.TestCase.fail;
import static open.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
@WebIntegrationTest("server.port=0")
public class PriorityQueueSchedulerServiceTest {

    private static final long TIMEOUT = 10000L;
    private static final long SUFFICIENT_WAIT = 2000L;

    private static final int NUMBER_OF_PRIORITY_LEVELS = 10;

    @Mock
    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    private PriorityQueueSchedulerService priorityQueueSchedulerService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        priorityQueueSchedulerService = new PriorityQueueSchedulerService(emailService, NUMBER_OF_PRIORITY_LEVELS);
    }

    @Test(timeout = TIMEOUT)
    public void testAnEmailIsScheduled() throws Exception {
        //Arrange
        final Email email = getSimpleMail();
        when(emailService.send(email)).thenReturn(mimeMessage);

        //Act
        priorityQueueSchedulerService.schedule(email, new Date(), 0);
        Thread.sleep(SUFFICIENT_WAIT);
        //Assert
        verify(emailService, times(1)).send(email);
    }

}