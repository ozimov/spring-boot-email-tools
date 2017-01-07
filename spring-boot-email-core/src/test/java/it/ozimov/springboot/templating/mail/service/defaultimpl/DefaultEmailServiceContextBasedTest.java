package it.ozimov.springboot.templating.mail.service.defaultimpl;


import it.ozimov.springboot.templating.mail.ContextBasedTest;
import it.ozimov.springboot.templating.mail.model.Email;
import it.ozimov.springboot.templating.mail.service.EmailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;

import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.getSimpleMailWithAttachments;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
public class DefaultEmailServiceContextBasedTest implements ContextBasedTest {

    @SpyBean
    public JavaMailSender javaMailSender;

    @Autowired
    public EmailService emailService;

    @Test
    public void sendMailWithoutTemplateButWithAttachmentsShouldCallJavaMailSender() throws Exception {
        //Arrange
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        final Email email = getSimpleMailWithAttachments();
        //Act
        MimeMessage givenMessage = emailService.send(email);

        //Assert
        assertThat(givenMessage, not(nullValue()));
        verify(javaMailSender).send(givenMessage);
    }

}