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

import com.google.common.collect.ImmutableMap;
import it.ozimov.springboot.templating.mail.model.Email;
import it.ozimov.springboot.templating.mail.model.InlinePicture;
import it.ozimov.springboot.templating.mail.service.EmailService;
import it.ozimov.springboot.templating.mail.service.TemplateService;
import it.ozimov.springboot.templating.mail.service.exception.CannotSendEmailException;
import it.ozimov.springboot.templating.mail.service.exception.TemplateException;
import it.ozimov.springboot.templating.mail.utils.EmailToMimeMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Optional.fromNullable;

@Service
@Slf4j
public class DefaultEmailService implements EmailService {

    private JavaMailSender javaMailSender;

    private TemplateService templateService;

    private EmailToMimeMessage emailToMimeMessage;

    @Autowired(required = false)
    public DefaultEmailService(final @NonNull JavaMailSender javaMailSender,
                               final TemplateService templateService,
                               final @NonNull EmailToMimeMessage emailToMimeMessage) {
        this.javaMailSender = javaMailSender;
        this.templateService = templateService;
        this.emailToMimeMessage = emailToMimeMessage;
    }

    @Autowired(required = false)
    public DefaultEmailService(final @NonNull JavaMailSender javaMailSender,
                               final @NonNull EmailToMimeMessage emailToMimeMessage) {
        this(javaMailSender, null, emailToMimeMessage);
    }

    @Override
    public MimeMessage send(final @NonNull Email email) {
        email.setSentAt(new Date());
        final MimeMessage mimeMessage = toMimeMessage(email);
        javaMailSender.send(mimeMessage);
        return mimeMessage;
    }

    public MimeMessage send(final @NonNull Email email,
                            final @NonNull String template,
                            final Map<String, Object> modelObject,
                            final @NonNull InlinePicture... inlinePictures) throws CannotSendEmailException {
        email.setSentAt(new Date());
        final MimeMessage mimeMessage = toMimeMessage(email);
        try {
            final MimeMultipart content = new MimeMultipart("related");

            String text = templateService.mergeTemplateIntoString(template,
                    fromNullable(modelObject).or(ImmutableMap.of()));

            for (final InlinePicture inlinePicture : inlinePictures) {
                final String cid = UUID.randomUUID().toString();

                //Set the cid in the template
                text = text.replace(inlinePicture.getTemplateName(), "cid:" + cid);

                //Set the image part
                final MimeBodyPart imagePart = new MimeBodyPart();
                imagePart.attachFile(inlinePicture.getFile());
                imagePart.setContentID('<' + cid + '>');
                imagePart.setDisposition(MimeBodyPart.INLINE);
                imagePart.setHeader("Content-Type", inlinePicture.getImageType().getContentType());
                content.addBodyPart(imagePart);
            }

            //Set the HTML text part
            final MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(text, email.getEncoding(), "html");
            content.addBodyPart(textPart);

            mimeMessage.setContent(content);
            javaMailSender.send(mimeMessage);
        } catch (IOException e) {
            log.error("The template file cannot be read", e);
            throw new CannotSendEmailException("Error while sending the email due to problems with the template file", e);
        } catch (TemplateException e) {
            log.error("The template file cannot be processed", e);
            throw new CannotSendEmailException("Error while processing the template file with the given model object", e);
        } catch (MessagingException e) {
            log.error("The mime message cannot be created", e);
            throw new CannotSendEmailException("Error while sending the email due to problems with the mime content", e);
        }
        return mimeMessage;
    }

    private MimeMessage toMimeMessage(@NotNull Email email) {
        return emailToMimeMessage.apply(email);
    }


}