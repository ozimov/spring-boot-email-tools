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

package open.springboot.mail.model;

import lombok.NonNull;
import open.springboot.mail.model.impl.EmailAttachmentImpl;

import javax.mail.internet.InternetAddress;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;

public interface Email {

    @NonNull InternetAddress getFrom();

    InternetAddress getReplyTo();

    Collection<InternetAddress> getTo();

    Collection<InternetAddress> getCc();

    Collection<InternetAddress> getBcc();

    default
    @NonNull
    String getSubject() {
        return "";
    }

    default
    @NonNull
    String getBody() {
        return "";
    }

    void setBody(@NonNull String body);

    @NonNull
    Collection<EmailAttachmentImpl> getAttachments();

    Charset getEncoding();

    Locale getLocale();

    LocalDate getSentAt();

    void setSentAt(@NonNull LocalDate sentAt);

    default boolean isHtmlRequested() {
        return false;
    }

    void setHtmlRequested();
}
