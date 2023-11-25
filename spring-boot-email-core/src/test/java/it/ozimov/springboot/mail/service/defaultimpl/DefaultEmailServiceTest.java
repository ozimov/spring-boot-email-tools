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


import com.google.common.collect.Maps;
import it.ozimov.springboot.mail.UnitTest;
import it.ozimov.springboot.mail.logging.EmailLogRenderer;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.ImageType;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultInlinePicture;
import it.ozimov.springboot.mail.service.TemplateService;
import it.ozimov.springboot.mail.service.exception.CannotSendEmailException;
import it.ozimov.springboot.mail.service.exception.TemplateException;
import it.ozimov.springboot.mail.utils.EmailToMimeMessage;
import it.ozimov.springboot.mail.utils.EmailToMimeMessageValidators;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;

import static it.ozimov.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEmailServiceTest extends EmailToMimeMessageValidators implements UnitTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateService templateService;

    @Mock
    private EmailLogRenderer emailLogRenderer;

    private EmailToMimeMessage emailToMimeMessage;

    private DefaultEmailService mailService;

    @Before
    public void setUp() {
        emailToMimeMessage = new EmailToMimeMessage(javaMailSender);
        when(emailLogRenderer.registerLogger(any(Logger.class))).thenReturn(emailLogRenderer);

        mailService = new DefaultEmailService(javaMailSender, templateService, emailToMimeMessage, emailLogRenderer);

        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendMailWithoutTemplate() throws MessagingException, IOException {
        //Arrange
        final Email email = getSimpleMail();
        assertThat(email.getSentAt(), is(nullValue()));

        //Act
        final MimeMessage sentMessage = mailService.send(email);

        //Assert
        assertThat(email.getSentAt(), not(is(nullValue())));
        validateFrom(email, sentMessage);
        validateReplyTo(email, sentMessage);
        validateTo(email, sentMessage);
        validateCc(email, sentMessage);
        validateBcc(email, sentMessage);
        validateSubject(email, sentMessage);
        validateBody(email, sentMessage);

        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void sendMailWithTemplate() throws MessagingException, IOException, TemplateException, CannotSendEmailException {
        //Arrange
        final Email email = getSimpleMail();
        assertThat(email.getSentAt(), is(nullValue()));
        final String toBeOverriddenBody = email.getBody();
        final String bodyToBeReturned = "Ciao Tito";
        when(templateService.mergeTemplateIntoString(any(String.class), any(Map.class))).thenReturn(bodyToBeReturned);

        //Act
        final MimeMessage sentMessage = mailService.send(email, "never_called.ftl", Maps.newHashMap());

        //Assert
        assertThat(email.getSentAt(), not(is(nullValue())));
        validateFrom(email, sentMessage);
        validateReplyTo(email, sentMessage);
        validateTo(email, sentMessage);
        validateCc(email, sentMessage);
        validateBcc(email, sentMessage);
        validateSubject(email, sentMessage);
        assertThat(((MimeMultipart) sentMessage.getContent()).getBodyPart(0).getContent(),
                allOf(not(is(toBeOverriddenBody)), is(bodyToBeReturned)));

        verify(templateService, times(1)).mergeTemplateIntoString(any(String.class), any(Map.class));

        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void sendMailWithTemplateAndInlinePicture() throws MessagingException, IOException, TemplateException, CannotSendEmailException, URISyntaxException {
        //Arrange
        final Email email = getSimpleMail();
        assertThat(email.getSentAt(), is(nullValue()));
        final String toBeOverriddenBody = email.getBody();
        final String bodyToBeReturned = "<img src=\"100_percent_free.jpg\" />";
        final String imageName = "100_percent_free.jpg";

        when(templateService.mergeTemplateIntoString(any(String.class), any(Map.class))).thenReturn(bodyToBeReturned);

        final File inlineImageFile = new File(getClass().getClassLoader()
                .getResource("images" + File.separator + imageName).toURI());

        //Act
        final MimeMessage sentMessage = mailService.send(email, "never_called.ftl", Maps.newHashMap(),
                getInlinePicture(inlineImageFile, imageName));

        //Assert
        assertThat(email.getSentAt(), not(is(nullValue())));
        validateFrom(email, sentMessage);
        validateReplyTo(email, sentMessage);
        validateTo(email, sentMessage);
        validateCc(email, sentMessage);
        validateBcc(email, sentMessage);
        validateSubject(email, sentMessage);

        final String imageId = ((MimeBodyPart) (((MimeMultipart) sentMessage.getContent()).getBodyPart(0))).getContentID();
        assertThat(((MimeMultipart) sentMessage.getContent()).getBodyPart(1).getContent(),
                allOf(not(is(toBeOverriddenBody)), not(is(bodyToBeReturned)),
                        is("<img src=\"cid:" +
                                imageId.substring(1, imageId.length() - 1)
                                + "\" />")));

        verify(templateService, times(1)).mergeTemplateIntoString(any(String.class), any(Map.class));
    }

    @Test
    public void sendMailWithoutTemplateShouldThrowExceptionWhenEmailIsNull() {
        //Arrange
        thrown.expect(NullPointerException.class);

        //Act
        mailService.send(null);

        //Assert
        fail();
    }

    @Test
    public void sendMailWithTemplateShouldThrowExceptionWhenEmailIsNull() throws CannotSendEmailException {
        //Arrange
        final String imageName = "100_percent_free.jpg";

        final File inlineImageFile = new File(getClass().getClassLoader()
                .getResource("images" + File.separator + imageName).getFile());

        thrown.expect(NullPointerException.class);

        //Act
        mailService.send(null, "never_called.ftl", Maps.newHashMap(),
                getInlinePicture(inlineImageFile, imageName));

        //Assert
        fail();
    }

    @Test
    public void sendMailWithTemplateShouldThrowExceptionWhenTemplateIsNull()
            throws CannotSendEmailException, UnsupportedEncodingException {
        //Arrange
        final Email email = getSimpleMail();
        final String imageName = "100_percent_free.jpg";

        final File inlineImageFile = new File(getClass().getClassLoader()
                .getResource("images" + File.separator + imageName).getFile());

        thrown.expect(NullPointerException.class);

        //Act
        mailService.send(email, null, Maps.newHashMap(),
                getInlinePicture(inlineImageFile, imageName));

        //Assert
        fail();
    }

    @Test
    public void sendMailWithTemplateAndInlinePictureThrowExceptionWhenPictureIsNull() throws IOException, CannotSendEmailException, TemplateException {
        //Arrange
        final Email email = getSimpleMail();
        assertThat(email.getSentAt(), is(nullValue()));

        when(templateService.mergeTemplateIntoString(any(String.class), any(Map.class))).thenReturn("doesn't matter");

        thrown.expect(NullPointerException.class);

        //Act
        mailService.send(email, "never_called.ftl", Maps.newHashMap(), null);

        //Assert
        fail();
    }

    @Test
    public void shouldSendMailWithTemplateCatchIOException() throws Exception {
        //Arrange
        final Email email = getSimpleMail();
        assertThat(email.getSentAt(), is(nullValue()));
        when(templateService.mergeTemplateIntoString(any(String.class), any(Map.class))).thenThrow(IOException.class);

        thrown.expect(CannotSendEmailException.class);
        thrown.expectMessage("Error while sending the email due to problems with the template file.");
        thrown.expectCause(instanceOf(IOException.class));
        //Act
        mailService.send(email, "never_called.ftl", Maps.newHashMap());

        //Assert
        fail();
    }

    @Test
    public void shouldSendMailWithTemplateCatchTemplateException() throws Exception {
        //Arrange
        final Email email = getSimpleMail();
        assertThat(email.getSentAt(), is(nullValue()));
        when(templateService.mergeTemplateIntoString(any(String.class), any(Map.class))).thenThrow(TemplateException.class);

        thrown.expect(CannotSendEmailException.class);
        thrown.expectMessage("Error while processing the template file with the given model object.");
        thrown.expectCause(instanceOf(TemplateException.class));

        //Act
        mailService.send(email, "never_called.ftl", Maps.newHashMap());

        //Assert
        fail();
    }

    @Test
    public void shouldSendMailWithTemplateCatchMessagingException() throws Exception {
        //Arrange
        final Email email = getSimpleMail();
        assertThat(email.getSentAt(), is(nullValue()));
        when(templateService.mergeTemplateIntoString(any(String.class), any(Map.class))).thenThrow(MessagingException.class);

        thrown.expect(CannotSendEmailException.class);
        thrown.expectMessage("Error while sending the email due to problems with the mime content.");
        thrown.expectCause(instanceOf(MessagingException.class));

        //Act
        mailService.send(email, "never_called.ftl", Maps.newHashMap());

        //Assert
        fail();
    }

    private it.ozimov.springboot.mail.model.InlinePicture getInlinePicture(final File inlineImageFile, final String imageName) {
        return DefaultInlinePicture.builder()
                .file(inlineImageFile)
                .imageType(ImageType.JPG)
                .templateName(imageName).build();
    }

}
