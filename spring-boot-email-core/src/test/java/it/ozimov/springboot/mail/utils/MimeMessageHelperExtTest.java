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

package it.ozimov.springboot.mail.utils;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import jakarta.mail.internet.MimeMessage;

import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class MimeMessageHelperExtTest {

    private static final String HEADER_DEPOSITION_NOTIFICATION_TO = "Disposition-Notification-To";

    private static final String HEADER_RETURN_RECEIPT = "Return-Receipt-To";

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Mock
    public MimeMessage mimeMessage;

    public static final String ENCODING = "UTF-16";

    @Test
    public void shouldConstructorWithMimeMessage() throws Exception {
        //Act
        MimeMessageHelperExt mimeMessageHelperExt = new MimeMessageHelperExt(mimeMessage);

        //Assert
        assertions.assertThat(mimeMessageHelperExt.getMimeMessage()).isEqualTo(mimeMessage);
    }

    @Test
    public void shouldConstructorWithMimeMessageAndEncoding() throws Exception {
        //Act
        MimeMessageHelperExt mimeMessageHelperExt = new MimeMessageHelperExt(mimeMessage, ENCODING);

        //Assert
        assertions.assertThat(mimeMessageHelperExt.getMimeMessage()).isEqualTo(mimeMessage);
        assertions.assertThat(mimeMessageHelperExt.getEncoding()).isEqualTo(ENCODING);
    }

    @Test
    public void shouldConstructorWithMimeMessageAndMultipart() throws Exception {
        //Act
        MimeMessageHelperExt mimeMessageHelperExt = new MimeMessageHelperExt(mimeMessage, false);
        MimeMessageHelperExt mimeMessageHelperExtMultipart = new MimeMessageHelperExt(mimeMessage, true);

        //Assert
        assertions.assertThat(mimeMessageHelperExt.getMimeMessage()).isEqualTo(mimeMessage);

        assertions.assertThat(mimeMessageHelperExt.isMultipart()).isFalse();
        assertions.assertThat(mimeMessageHelperExtMultipart.isMultipart()).isTrue();
    }

    @Test
    public void shouldConstructorWithMimeMessageMultipartAndEncding() throws Exception {
        //Act
        MimeMessageHelperExt mimeMessageHelperExt = new MimeMessageHelperExt(mimeMessage, false, ENCODING);
        MimeMessageHelperExt mimeMessageHelperExtMultipart = new MimeMessageHelperExt(mimeMessage, true, ENCODING);

        //Assert
        assertions.assertThat(mimeMessageHelperExt.getMimeMessage()).isEqualTo(mimeMessage);
        assertions.assertThat(mimeMessageHelperExt.getEncoding()).isEqualTo(ENCODING);

        assertions.assertThat(mimeMessageHelperExt.isMultipart()).isFalse();
        assertions.assertThat(mimeMessageHelperExtMultipart.isMultipart()).isTrue();
    }


    @Test
    public void shouldConstructorWithMimeMessageMultipartMode() throws Exception {
        //Act
        MimeMessageHelperExt mimeMessageHelperExtMixed = new MimeMessageHelperExt(mimeMessage, 1);
        MimeMessageHelperExt mimeMessageHelperExtRelated = new MimeMessageHelperExt(mimeMessage, 2);

        //Assert
        assertions.assertThat(mimeMessageHelperExtMixed.getMimeMessage()).isEqualTo(mimeMessage);
        assertions.assertThat(mimeMessageHelperExtMixed.getRootMimeMultipart().getContentType()).containsIgnoringCase("multipart/mixed");

        assertions.assertThat(mimeMessageHelperExtRelated.getRootMimeMultipart().getContentType()).containsIgnoringCase("multipart/related");
    }

    @Test
    public void shouldConstructorWithMimeMessageMultipartModeAndEncding() throws Exception {
        //Act
        MimeMessageHelperExt mimeMessageHelperExtMixed = new MimeMessageHelperExt(mimeMessage, 1, ENCODING);
        MimeMessageHelperExt mimeMessageHelperExtRelated = new MimeMessageHelperExt(mimeMessage, 2, ENCODING);

        //Assert
        assertions.assertThat(mimeMessageHelperExtMixed.getMimeMessage()).isEqualTo(mimeMessage);
        assertions.assertThat(mimeMessageHelperExtMixed.getRootMimeMultipart().getContentType()).containsIgnoringCase("multipart/mixed");
        assertions.assertThat(mimeMessageHelperExtMixed.getEncoding()).isEqualTo(ENCODING);

        assertions.assertThat(mimeMessageHelperExtRelated.getRootMimeMultipart().getContentType()).containsIgnoringCase("multipart/related");
    }

    @Test
    public void shouldConstructorsWork() throws Exception {
        //Act
        MimeMessageHelperExt mimeMessageHelperExt5 = new MimeMessageHelperExt(mimeMessage, 1);
        MimeMessageHelperExt mimeMessageHelperExt6 = new MimeMessageHelperExt(mimeMessage, 1, "UTF-8");

        //Assert

//        assertions.assertThat(mimeMessageHelperExt1).is
    }

    @Test
    public void shouldSetHeaderReturnReceipt() throws Exception {
        //Arrange
        MimeMessageHelperExt mimeMessageHelperExt = new MimeMessageHelperExt(mimeMessage);
        String expectedReturnReceipt = "marco-tullio@roma.aeterna";

        //Act
        mimeMessageHelperExt.setHeaderReturnReceipt(expectedReturnReceipt);

        //Assert
        verify(mimeMessage).setHeader(HEADER_RETURN_RECEIPT, expectedReturnReceipt);
    }

    @Test
    public void shouldSetHeaderDepositionNotificationTo() throws Exception {
        //Arrange
        MimeMessageHelperExt mimeMessageHelperExt = new MimeMessageHelperExt(mimeMessage);
        String expectedDepositionNotificationTo = "marco-tullio@roma.aeterna";

        //Act
        mimeMessageHelperExt.setHeaderDepositionNotificationTo(expectedDepositionNotificationTo);

        //Assert
        verify(mimeMessage).setHeader(HEADER_DEPOSITION_NOTIFICATION_TO, expectedDepositionNotificationTo);
    }

}