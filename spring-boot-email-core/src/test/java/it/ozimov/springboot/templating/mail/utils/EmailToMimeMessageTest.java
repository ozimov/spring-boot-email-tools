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

import com.google.common.collect.Lists;
import it.ozimov.springboot.templating.mail.model.Email;
import it.ozimov.springboot.templating.mail.model.impl.EmailAttachmentImpl;
import it.ozimov.springboot.templating.mail.model.impl.EmailImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static javax.mail.Message.RecipientType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailToMimeMessageTest {


    private static final String HEADER_DEPOSITION_NOTIFICATION_TO = "Disposition-Notification-To";

    private static final String HEADER_RETURN_RECEIPT = "Return-Receipt-To";

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailToMimeMessage emailToMimeMessage;

    public static void validateFrom(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> froms = asList(sentMessage.getFrom());
        assertThat(froms, hasSize(1)); // redundant with contains
        assertThat(froms, contains((Address) email.getFrom()));
    }

    public static void validateReplyTo(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> replyTos = asList(sentMessage.getReplyTo());
        assertThat(replyTos, hasSize(1)); // redundant with contains
        assertThat(replyTos, contains((Address) email.getReplyTo()));
    }

    public static void validateTo(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> tos = asList(sentMessage.getRecipients(TO));
        assertThat(tos.get(0), is((Address) (new ArrayList<>(email.getTo()).get(0))));
        assertThat(tos, everyItem(is(in(toAddress(email.getTo())))));
    }

    public static void validateCc(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> ccs = asList(sentMessage.getRecipients(CC));
        assertThat(ccs, everyItem(is(in(toAddress(email.getCc())))));
    }

    public static void validateBcc(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> bccs = asList(sentMessage.getRecipients(BCC));
        assertThat(bccs, everyItem(is(in(toAddress(email.getBcc())))));
    }

    public static void validateSubject(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        assertThat(sentMessage.getSubject(), is(email.getSubject()));
    }

    public static void validateBody(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        assertThat(sentMessage.getContent(), is(email.getBody()));
    }

    //FIXME In release 1.4.0 must be EmailAttachment
    public static Email getSimpleMail(InternetAddress from, EmailAttachmentImpl ... emailAttachments) throws UnsupportedEncodingException {
        EmailImpl.EmailImplBuilder builder = EmailImpl.builder()
                .from(from)
                .replyTo(new InternetAddress("tullius.cicero@urbs.aeterna", "Marcus Tullius Cicero"))
                .to(Lists.newArrayList(new InternetAddress("roberto.trunfio@gmail.com", "titus@de-rerum.natura", "Pomponius Attĭcus")))
                .cc(Lists.newArrayList(new InternetAddress("tito55@de-rerum.natura", "Titus Lucretius Carus"),
                        new InternetAddress("info@de-rerum.natura", "Info Best Seller")))
                .bcc(Lists.newArrayList(new InternetAddress("caius-memmius@urbs.aeterna", "Caius Memmius")))
                .depositionNotificationTo(new InternetAddress("caligola@urbs.aeterna", "Gaius Iulius Caesar Augustus Germanicus"))
                .receiptTo(new InternetAddress("caligola@urbs.aeterna", "Gaius Iulius Caesar Augustus Germanicus"))
                .subject("Laelius de amicitia")
                .body("Firmamentum autem stabilitatis constantiaeque eius, quam in amicitia quaerimus, fides est.")
                .encoding(Charset.forName("UTF-8"));
        if(nonNull(emailAttachments)){
            builder.attachments(Arrays.asList(emailAttachments));
        }
        return builder.build();
    }

    public static Email getSimpleMail() throws UnsupportedEncodingException {
        return getSimpleMail(new InternetAddress("cicero@mala-tempora.currunt", "Marco Tullio Cicerone"));
    }

    public static Email getSimpleMailWithAttachments() throws UnsupportedEncodingException {
        return getSimpleMail(new InternetAddress("cicero@mala-tempora.currunt", "Marco Tullio Cicerone"),
                getCsvAttachment("test1"), getCsvAttachment("test2"));
    }

    private static List<Address> toAddress(final Collection<InternetAddress> internetAddresses) {
        return internetAddresses.stream().map(internetAddress -> (Address) internetAddress).collect(toList());
    }

    @Test
    public void sendMailWithoutTemplate() throws MessagingException, IOException {

        // Arrange
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        final Email email = getSimpleMail();

        // Act
        final MimeMessage sentMessage = emailToMimeMessage.apply(email);

        // Assert
        validateFrom(email, sentMessage);
        validateReplyTo(email, sentMessage);
        validateTo(email, sentMessage);
        validateCc(email, sentMessage);
        validateBcc(email, sentMessage);
        validateDepositionNotification(email, sentMessage);
        validateReceipt(email, sentMessage);
        validateSubject(email, sentMessage);
        validateBody(email, sentMessage);

        verify(javaMailSender).createMimeMessage();
    }

    private void validateReceipt(Email email, MimeMessage sentMessage) throws MessagingException {
        assertThat(sentMessage.getHeader(HEADER_RETURN_RECEIPT)[0], is(email.getReceiptTo().getAddress()));
    }

    private void validateDepositionNotification(Email email, MimeMessage sentMessage) throws MessagingException {
        assertThat(sentMessage.getHeader(HEADER_DEPOSITION_NOTIFICATION_TO)[0], is(email.getReceiptTo().getAddress()));
    }

    //FIXME In release 1.4.0 must be EmailAttachment
    private static EmailAttachmentImpl getCsvAttachment(String filename) {
        final String testData = "col1,col2\n1,2\n3,4";
        final it.ozimov.springboot.templating.mail.model.impl.EmailAttachmentImpl attachment = it.ozimov.springboot.templating.mail.model.impl.EmailAttachmentImpl.builder()
                .attachmentName(filename+".csv")
                .attachmentData(testData.getBytes(Charset.forName("UTF-8")))
                .mediaType(MediaType.TEXT_PLAIN).build();
        return attachment;
    }

}
