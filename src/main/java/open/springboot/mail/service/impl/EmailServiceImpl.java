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

import freemarker.template.TemplateException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import open.springboot.mail.model.Email;
import open.springboot.mail.service.*;
import open.springboot.mail.service.Exception.CannotSendEmailException;
import open.springboot.mail.utils.EmailToMimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author rtrunfio
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

//    @Resource(name="mailSender")
    private JavaMailSender javaMailSender;

    private TemplateService templateService;

    private EmailToMimeMessage emailToMimeMessage;

    @Autowired
    public EmailServiceImpl(final @NonNull JavaMailSender javaMailSender,
                            final @NonNull TemplateService templateService,
                            final @NonNull EmailToMimeMessage emailToMimeMessage) {
        this.javaMailSender = javaMailSender;
        this.templateService = templateService;
        this.emailToMimeMessage = emailToMimeMessage;
    }

    @Override
    public MimeMessage send(final @NonNull Email email) {
        email.setSentAt(LocalDate.now());
        final MimeMessage mimeMessage = toMimeMessage(email);
        javaMailSender.send(mimeMessage);
        return mimeMessage;
    }

    public MimeMessage send(final @NonNull Email email,
                            final @NonNull String template,
                            final @NonNull Map<String, Object> modelObject) throws CannotSendEmailException {
        try {
            setBodyFromTemplate(email, template, modelObject);
        } catch (IOException e) {
            log.error("The template file cannot be read", e);
            throw new CannotSendEmailException("Error while sending the email due to problems with the template file", e);
        } catch (TemplateException e) {
            log.error("The template file cannot be processed", e);
            throw new CannotSendEmailException("Error while processing the template file with the given model object", e);
        }
        return send(email);
    }

    private void setBodyFromTemplate(final Email email,
                                     final String template,
                                     final Map<String, Object> modelObject) throws IOException, TemplateException {
        email.setBody(templateService.mergeTemplateIntoString(template, modelObject));
    }

    private MimeMessage toMimeMessage(@NotNull Email email) {
        return emailToMimeMessage.apply(email);
    }

}