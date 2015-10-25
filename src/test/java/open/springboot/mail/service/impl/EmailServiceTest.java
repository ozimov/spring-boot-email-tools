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

package open.springboot.mail.service.impl;


import com.google.common.collect.Maps;
import freemarker.template.TemplateException;
import open.springboot.mail.model.Email;
import open.springboot.mail.service.Exception.CannotSendEmailException;
import open.springboot.mail.service.TemplateService;
import open.springboot.mail.service.impl.EmailServiceImpl;
import open.springboot.mail.utils.EmailToMimeMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static open.springboot.mail.utils.EmailToMimeMessageTest.assertMimeMessageHasProperValues;
import static open.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;
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
public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateService templateService;

    private EmailToMimeMessage emailToMimeMessage;

    private EmailServiceImpl mailService;

    @Before
    public void setUp() {
        emailToMimeMessage = new EmailToMimeMessage(javaMailSender);
        mailService = new EmailServiceImpl(javaMailSender, templateService, emailToMimeMessage);

        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
    }

    @After
    public void setDown() {
        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
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
        assertMimeMessageHasProperValues(email, sentMessage);
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
        assertThat(email.getBody(), allOf(not(is(toBeOverriddenBody)), is(bodyToBeReturned)));
        assertMimeMessageHasProperValues(email, sentMessage);

        verify(templateService, times(1)).mergeTemplateIntoString(any(String.class), any(Map.class));
    }


}