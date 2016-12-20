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


import com.google.common.collect.Maps;
import it.ozimov.springboot.templating.mail.UnitTest;
import it.ozimov.springboot.templating.mail.model.Email;
import it.ozimov.springboot.templating.mail.model.ImageType;
import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultInlinePicture;
import it.ozimov.springboot.templating.mail.service.TemplateService;
import it.ozimov.springboot.templating.mail.service.defaultimpl.DefaultEmailService;
import it.ozimov.springboot.templating.mail.service.exception.CannotSendEmailException;
import it.ozimov.springboot.templating.mail.service.exception.TemplateException;
import it.ozimov.springboot.templating.mail.utils.EmailToMimeMessage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.getSimpleMail;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.validateBcc;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.validateBody;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.validateCc;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.validateFrom;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.validateReplyTo;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.validateSubject;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.validateTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEmailServiceTest implements UnitTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateService templateService;

    private EmailToMimeMessage emailToMimeMessage;

    private DefaultEmailService mailService;

    @Before
    public void setUp() {
        emailToMimeMessage = new EmailToMimeMessage(javaMailSender);
        mailService = new DefaultEmailService(javaMailSender, templateService, emailToMimeMessage);

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
    public void sendMailWithTemplateAndInlinePicture() throws MessagingException, IOException, TemplateException, CannotSendEmailException {
        //Arrange
        final Email email = getSimpleMail();
        assertThat(email.getSentAt(), is(nullValue()));
        final String toBeOverriddenBody = email.getBody();
        final String bodyToBeReturned = "<img src=\"100_percent_free.jpg\" />";
        final String imageName = "100_percent_free.jpg";

        when(templateService.mergeTemplateIntoString(any(String.class), any(Map.class))).thenReturn(bodyToBeReturned);

        final File inlineImageFile = new File(getClass().getClassLoader()
                .getResource("images" + File.separator + imageName).getFile());

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
    public void sendMailWithoutTemplateShouldThrowWhenEmailIsNull() {
        //Arrange
        thrown.expect(NullPointerException.class);

        //Act
        mailService.send(null);
    }

    @Test
    public void sendMailWithTemplateShouldThrowWhenEmailIsNull() throws CannotSendEmailException {
        //Arrange
        thrown.expect(NullPointerException.class);
        final String imageName = "100_percent_free.jpg";

        final File inlineImageFile = new File(getClass().getClassLoader()
                .getResource("images" + File.separator + imageName).getFile());

        //Act
        mailService.send(null, "never_called.ftl", Maps.newHashMap(),
                getInlinePicture(inlineImageFile, imageName));
    }

    @Test
    public void sendMailWithTemplateShouldThrowWhenTemplateIsNull()
            throws CannotSendEmailException, UnsupportedEncodingException {
        //Arrange
        thrown.expect(NullPointerException.class);
        final Email email = getSimpleMail();
        final String imageName = "100_percent_free.jpg";

        final File inlineImageFile = new File(getClass().getClassLoader()
                .getResource("images" + File.separator + imageName).getFile());

        //Act
        mailService.send(email, null, Maps.newHashMap(),
                getInlinePicture(inlineImageFile, imageName));
    }

    @Test
    public void sendMailWithTemplateAndInlinePictureThrowWhenPictureIsNull() throws IOException, CannotSendEmailException, TemplateException {
        //Arrange
        thrown.expect(NullPointerException.class);
        final Email email = getSimpleMail();
        assertThat(email.getSentAt(), is(nullValue()));

        when(templateService.mergeTemplateIntoString(any(String.class), any(Map.class))).thenReturn("doesn't matter");

        //Act
        mailService.send(email, "never_called.ftl", Maps.newHashMap(), null);
    }

    private it.ozimov.springboot.templating.mail.model.InlinePicture getInlinePicture(final File inlineImageFile, final String imageName) {
        return DefaultInlinePicture.builder()
                .file(inlineImageFile)
                .imageType(ImageType.JPG)
                .templateName(imageName).build();
    }

}
