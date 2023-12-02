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

package it.ozimov.springboot.mail.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.ozimov.springboot.mail.utils.StringUtils;
import lombok.NonNull;

import jakarta.mail.internet.InternetAddress;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public interface Email extends Serializable {

    @NonNull
    InternetAddress getFrom();

    InternetAddress getReplyTo();

    @NonNull
    Collection<InternetAddress> getTo();

    @NonNull
    default Collection<InternetAddress> getCc() {
        return ImmutableList.of();
    }

    @NonNull
    default Collection<InternetAddress> getBcc() {
        return ImmutableList.of();
    }

    @NonNull
    default String getSubject() {
        return StringUtils.EMPTY;
    }

    @NonNull
    default String getBody() {
        return StringUtils.EMPTY;
    }

    @NonNull
    default Collection<EmailAttachment> getAttachments() {
        return ImmutableList.of();
    }

    /**
     * Return the charset encoding. Default value is UTF-8
     */
    //Observe that Charset does not guarantee that the object is Serializable, therefore we may break serialization
    String getEncoding();

    Locale getLocale();

    Date getSentAt();

    void setSentAt(Date sentAt);

    InternetAddress getReceiptTo();

    InternetAddress getDepositionNotificationTo();

    @NonNull
    default Map<String, String> getCustomHeaders() {
        return ImmutableMap.of();
    }

}