package it.ozimov.springboot.mail.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import jakarta.mail.internet.InternetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public class EmailTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldGetSubjectReturnDefaultValue() throws Exception {
        //Arrange
        Email email = new DummyEmail();

        //Act
        String givenSubject = email.getSubject();

        //Assert
        assertions.assertThat(givenSubject).isEmpty();
    }

    @Test
    public void shouldGetBodyReturnDefaultValue() throws Exception {
        //Arrange
        Email email = new DummyEmail();

        //Act
        String givenBody = email.getBody();

        //Assert
        assertions.assertThat(givenBody).isEmpty();
    }


    public class DummyEmail implements Email {

        @Override
        public InternetAddress getFrom() {
            throw new UnsupportedOperationException();
        }

        @Override
        public InternetAddress getReplyTo() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<InternetAddress> getTo() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<InternetAddress> getCc() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<InternetAddress> getBcc() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<EmailAttachment> getAttachments() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getEncoding() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Date getSentAt() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSentAt(Date sentAt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InternetAddress getReceiptTo() {
            throw new UnsupportedOperationException();
        }

        @Override
        public InternetAddress getDepositionNotificationTo() {
            throw new UnsupportedOperationException();
        }
    }

}