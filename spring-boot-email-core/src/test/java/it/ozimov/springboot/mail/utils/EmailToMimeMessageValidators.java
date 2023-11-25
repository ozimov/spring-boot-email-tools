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

import it.ozimov.springboot.mail.model.Email;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;

import jakarta.mail.Address;
import jakarta.mail.Header;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static jakarta.mail.Message.RecipientType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EmailToMimeMessageValidators {

    public static final String HEADER_DEPOSITION_NOTIFICATION_TO = "Disposition-Notification-To";

    public static final String HEADER_RETURN_RECEIPT = "Return-Receipt-To";

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    public void validateFrom(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> froms = asList(sentMessage.getFrom());
        assertThat(froms, hasSize(1)); // redundant with contains
        assertThat(froms, contains((Address) email.getFrom()));
    }

    public void validateReplyTo(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> replyTos = asList(sentMessage.getReplyTo());
        assertThat(replyTos, hasSize(1)); // redundant with contains
        assertThat(replyTos, contains((Address) email.getReplyTo()));
    }

    public void validateTo(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> tos = asList(sentMessage.getRecipients(TO));
        assertThat(tos.get(0), is((new ArrayList<>(email.getTo()).get(0))));
        assertThat(tos, everyItem(is(in(toAddress(email.getTo())))));
    }

    public void validateCc(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> ccs = asList(sentMessage.getRecipients(CC));
        assertThat(ccs, everyItem(is(in(toAddress(email.getCc())))));
    }

    public void validateBcc(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> bccs = asList(sentMessage.getRecipients(BCC));
        assertThat(bccs, everyItem(is(in(toAddress(email.getBcc())))));
    }

    public void validateReceipt(Email email, MimeMessage sentMessage) throws MessagingException {
        assertThat(sentMessage.getHeader(HEADER_RETURN_RECEIPT)[0], is(email.getReceiptTo().getAddress()));
    }

    public void validateDepositionNotification(Email email, MimeMessage sentMessage) throws MessagingException {
        assertThat(sentMessage.getHeader(HEADER_DEPOSITION_NOTIFICATION_TO)[0], is(email.getReceiptTo().getAddress()));
    }

    public void validateSubject(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        assertThat(sentMessage.getSubject(), is(email.getSubject()));
    }

    public void validateBody(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        assertThat(sentMessage.getContent(), is(email.getBody()));
    }

    public void validateCustomHeaders(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        Map<String, String> customHeaders = email.getCustomHeaders();
        List<Header> internetHeaders = (List<Header>) Collections.list(sentMessage.getAllHeaders());
        List<String> headerKeys = internetHeaders.stream().map(Header::getName).collect(toList());

        assertions.assertThat(headerKeys)
                .as("Should contains all the headers keys provided at construction time")
                .containsAll(customHeaders.keySet());

        customHeaders.entrySet().stream()
                .forEach(entry -> {
                    try {
                        assertions.assertThat(sentMessage.getHeader(entry.getKey())).isNotNull().containsExactly(entry.getValue());
                    } catch (MessagingException e) {
                    }
                });
    }

    private static List<Address> toAddress(final Collection<InternetAddress> internetAddresses) {
        return internetAddresses.stream().map(internetAddress -> (Address) internetAddress).collect(toList());
    }

}
