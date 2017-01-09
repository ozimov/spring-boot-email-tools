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

package it.ozimov.springboot.templating.mail.utils;

import it.ozimov.springboot.templating.mail.exceptions.EmailConversionException;
import it.ozimov.springboot.templating.mail.model.Email;
import it.ozimov.springboot.templating.mail.model.impl.EmailAttachmentImpl;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

import static com.google.common.base.Optional.fromNullable;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class EmailToMimeMessage implements Function<Email, MimeMessage> {

    private static final String EMPTY_STRING = "";
    
    private JavaMailSender javaMailSender;

    @Autowired
    public EmailToMimeMessage(final @NonNull JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public MimeMessage apply(final Email email) {
        final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        final boolean isMultipart = nonNull(email.getAttachments()) && !email.getAttachments().isEmpty();

        try {
            final MimeMessageHelperExt messageHelper = new MimeMessageHelperExt(mimeMessage,
                    isMultipart,
                    fromNullable(email.getEncoding()).or(Charset.forName("UTF-8")).displayName());

            messageHelper.setFrom(email.getFrom());
            if (nonNull(email.getReplyTo())) {
                messageHelper.setReplyTo(email.getReplyTo());
            }
            if (nonNull(email.getTo())) {
                for (final InternetAddress address : email.getTo()) {
                    messageHelper.addTo(address);
                }
            }
            if (nonNull(email.getCc())) {
                for (final InternetAddress address : email.getCc()) {
                    messageHelper.addCc(address);
                }
            }
            if (nonNull(email.getBcc())) {
                for (final InternetAddress address : email.getBcc()) {
                    messageHelper.addBcc(address);
                }
            }
            if (isMultipart) {
                for (final EmailAttachmentImpl attachment : email.getAttachments()) {
                    messageHelper.addAttachment(attachment.getAttachmentName(), attachment.getInputStream());
                }
            }
            messageHelper.setSubject(ofNullable(email.getSubject()).orElse(EMPTY_STRING));
            messageHelper.setText(ofNullable(email.getBody()).orElse(EMPTY_STRING));

            if (nonNull(email.getSentAt())) {
                messageHelper.setSentDate(email.getSentAt());
            }

            if (nonNull(email.getReceiptTo())) {
                messageHelper.setHeaderDepositionNotificationTo(email.getReceiptTo().getAddress());
            }

            if (nonNull(email.getDepositionNotificationTo())) {
                messageHelper.setHeaderReturnReceipt(email.getDepositionNotificationTo().getAddress());
            }

        } catch (MessagingException e) {
            log.error("Error while converting Email to MimeMessage");
            throw new EmailConversionException(e);
        }

        return mimeMessage;
    }


}