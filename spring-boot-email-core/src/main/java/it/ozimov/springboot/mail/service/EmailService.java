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

package it.ozimov.springboot.mail.service;

import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.InlinePicture;
import it.ozimov.springboot.mail.service.exception.CannotSendEmailException;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;

public interface EmailService {

    /**
     * Send an email message.
     * <p>
     * The send date is set or overridden if any is present.
     *
     * @param mimeEmail an email to be send
     */
    MimeMessage send(Email mimeEmail);

    /**
     * Send an email message.
     * <p>
     * The body is ignored if present.
     * The send date is set or overridden if any is present.
     *
     * @param mimeEmail      an email to be send
     * @param template       the reference to the template file
     * @param modelObject    the model object to be used for the template engine, it may be null
     * @param inlinePictures list of pictures to be rendered inline in the template
     */
    MimeMessage send(Email mimeEmail,
                     String template, Map<String, Object> modelObject,
                     InlinePicture... inlinePictures) throws CannotSendEmailException;

}
