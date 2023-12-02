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

import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class MimeMessageHelperExt extends MimeMessageHelper {

    private static final String HEADER_RETURN_RECEIPT = "Return-Receipt-To";

    private static final String HEADER_DEPOSITION_NOTIFICATION_TO = "Disposition-Notification-To";

    public MimeMessageHelperExt(MimeMessage mimeMessage) {
        super(mimeMessage);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, String encoding) {
        super(mimeMessage, encoding);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, boolean multipart) throws MessagingException {
        super(mimeMessage, multipart);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, boolean multipart, String encoding) throws MessagingException {
        super(mimeMessage, multipart, encoding);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, int multipartMode) throws MessagingException {
        super(mimeMessage, multipartMode);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, int multipartMode, String encoding) throws MessagingException {
        super(mimeMessage, multipartMode, encoding);
    }

    public void setHeaderReturnReceipt(String emailToNotification) throws MessagingException {
        getMimeMessage().setHeader(HEADER_RETURN_RECEIPT, emailToNotification);
    }

    public void setHeaderDepositionNotificationTo(String emailToNotification) throws MessagingException {
        getMimeMessage().setHeader(HEADER_DEPOSITION_NOTIFICATION_TO, emailToNotification);
    }

}