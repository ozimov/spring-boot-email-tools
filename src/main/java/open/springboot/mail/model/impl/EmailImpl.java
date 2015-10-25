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

package open.springboot.mail.model.impl;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import open.springboot.mail.model.Email;

import javax.mail.internet.InternetAddress;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;

import static java.nio.charset.Charset.forName;
import static java.util.Objects.nonNull;

/**
 * Mime email.
 */
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Slf4j
public class EmailImpl implements Email, Serializable {

    private static final long serialVersionUID = 634175529482595823L;

    @NonNull
    private InternetAddress from;

    private InternetAddress replyTo;

    private Collection<InternetAddress> to;

    private Collection<InternetAddress> cc;

    private Collection<InternetAddress> bcc;

    private @NonNull String subject;

    private @NonNull String body;

    private Collection<EmailAttachmentImpl> attachments;

    private Charset encoding = forName("UTF-8");

    private Locale locale;

    private LocalDate sentAt;

    private boolean htmlRequested;

    @Override
    public void setHtmlRequested() {
        setHtmlRequested(true);
    }
}


