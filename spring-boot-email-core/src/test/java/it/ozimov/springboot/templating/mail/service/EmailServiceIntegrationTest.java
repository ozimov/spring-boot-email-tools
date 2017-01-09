package it.ozimov.springboot.templating.mail.service;


import it.ozimov.springboot.templating.mail.model.Email;
import it.ozimov.springboot.templating.mail.service.exception.TemplateException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Map;

import static it.ozimov.springboot.templating.mail.utils.EmailToMimeMessageTest.getSimpleMailWithAttachments;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
public class EmailServiceIntegrationTest {

    @SpyBean
    public JavaMailSender javaMailSender;

    @MockBean
    public TemplateService templateService;

    @Autowired
    public EmailService emailService;

    @Before
    public void setUp() throws IOException, TemplateException {
        when(templateService.mergeTemplateIntoString(anyString(), any(Map.class)))
                .thenReturn( "<!doctype html>\n" +
                        "<html>\n" +
                        "<body>\n" +
                        "<p>\n" +
                        "    THIS IS A TEST WITH TEMPLATE\n" +
                        "</p>\n" +
                        "</body>\n" +
                        "</html>");
    }

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

    @Test
    public void sendMailWithTemplateButWithAttachmentsShouldCallJavaMailSender() throws Exception {
        //Arrange
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        final Email email = getSimpleMailWithAttachments();

        //Act
        MimeMessage givenMessage = emailService.send(email, TemplatingTestUtils.TEMPLATE, TemplatingTestUtils.MODEL_OBJECT);

        //Assert
        assertThat(givenMessage, not(nullValue()));
        verify(javaMailSender).send(givenMessage);
    }

}