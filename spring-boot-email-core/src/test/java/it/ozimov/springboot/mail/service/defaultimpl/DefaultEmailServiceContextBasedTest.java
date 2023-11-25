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


import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import it.ozimov.springboot.mail.ContextBasedTest;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.service.EmailService;
import it.ozimov.springboot.mail.service.TemplateService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.SPRING_MAIL_PORT;
import static it.ozimov.springboot.mail.service.defaultimpl.DefaultEmailServiceContextBasedTest.MAIL_PORT;
import static it.ozimov.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;
import static it.ozimov.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMailWithAttachments;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties",
        properties = SPRING_MAIL_PORT + "=" + MAIL_PORT)
public class DefaultEmailServiceContextBasedTest implements ContextBasedTest {

    public static final int MAIL_PORT = 3026;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @SpyBean
    public JavaMailSender javaMailSender;

    private GreenMail testSmtp;

    @MockBean
    private TemplateService templateService;

    @Autowired
    private EmailService emailService;

    @Before
    public void setUp() throws Exception {
        when(templateService.mergeTemplateIntoString(anyString(), any(Map.class)))
                .thenReturn("<!doctype html>\n" +
                        "<html>\n" +
                        "<body>\n" +
                        "<p>\n" +
                        "    THIS IS A TEST WITH TEMPLATE\n" +
                        "</p>\n" +
                        "</body>\n" +
                        "</html>");

        ServerSetup serverSetup = new ServerSetup(MAIL_PORT, (String) null, "smtp");
        testSmtp = new GreenMail(serverSetup);
        testSmtp.start();

        //don't forget to set the test port!
        ((JavaMailSenderImpl) javaMailSender).setPort(serverSetup.getPort());
        ((JavaMailSenderImpl) javaMailSender).setHost("localhost");
    }

    @After
    public void cleanup() {
        testSmtp.stop();
    }

    @Test
    public void sendMailWithoutTemplateAndWithoutAttachmentsShouldCallJavaMailSender() throws Exception {
        //Arrange
        final Email email = getSimpleMail();

        //Act
        MimeMessage givenMessage = emailService.send(email);

        //Assert
        assertThat(givenMessage, not(nullValue()));
        verify(javaMailSender).send(givenMessage);
    }

    @Test
    public void sendMailWithTemplateAndWithoutAttachmentsShouldCallJavaMailSender() throws Exception {
        //Arrange
        final Email email = getSimpleMail();

        //Act
        MimeMessage givenMessage = emailService.send(email, TemplatingTestUtils.TEMPLATE, TemplatingTestUtils.MODEL_OBJECT);

        //Assert
        assertThat(givenMessage, not(nullValue()));
        verify(javaMailSender).send(givenMessage);
    }

    @Test
    public void sendMailWithoutTemplateButWithAttachmentsShouldCallJavaMailSender() throws Exception {
        //Arrange
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
        final Email email = getSimpleMailWithAttachments();

        //Act
        MimeMessage givenMessage = emailService.send(email, TemplatingTestUtils.TEMPLATE, TemplatingTestUtils.MODEL_OBJECT);

        //Assert
        assertThat(givenMessage, not(nullValue()));
        verify(javaMailSender).send(givenMessage);
    }

}