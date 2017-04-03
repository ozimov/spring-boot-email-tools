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

package it.ozimov.springboot.mail.model.defaultimpl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.EmailAttachment;
import lombok.*;

import javax.mail.internet.InternetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Mime email.
 */
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DefaultEmail implements Email {

    private static final long serialVersionUID = 634175529482595823L;

    @NonNull
    private InternetAddress from;

    private InternetAddress replyTo;

    private Collection<InternetAddress> to;

    private Collection<InternetAddress> cc;

    private Collection<InternetAddress> bcc;

    @NonNull
    private String subject;

    @NonNull
    private String body;

    @NonNull
    @Singular
    private Collection<EmailAttachment> attachments;

    private String encoding = StandardCharsets.UTF_8.name();

    private Locale locale;

    private Date sentAt;

    private InternetAddress receiptTo;

    private InternetAddress depositionNotificationTo;

    private Map<String, String> customHeaders;

    //This is to have default values in Lombok constructor
    public static class DefaultEmailBuilder {

        private Collection<InternetAddress> cc = ImmutableList.of();

        private Collection<InternetAddress> bcc = ImmutableList.of();

        private String encoding = StandardCharsets.UTF_8.name();

        private Map<String, String> customHeaders = ImmutableMap.of();
    }

}