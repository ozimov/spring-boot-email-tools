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

package it.ozimov.springboot.templating.mail.model;

import javax.mail.internet.InternetAddress;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public interface Email extends Serializable {

    InternetAddress getFrom();

    InternetAddress getReplyTo();

    Collection<InternetAddress> getTo();

    Collection<InternetAddress> getCc();

    Collection<InternetAddress> getBcc();

    default String getSubject() {
        return "";
    }

    default String getBody() {
        return "";
    }

    Collection<EmailAttachment> getAttachments();

    /**
     * Return the charset encoding. Default value is UTF-8
     */
    //Observe that Charset does not guarantee that the object is Serializable, therefore we may break serialization
    String getEncoding();

    Locale getLocale();

    Date getSentAt();

    void setSentAt(Date sentAt);

}