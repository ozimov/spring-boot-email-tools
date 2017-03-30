package it.ozimov.springboot.mail.configuration;

import it.ozimov.springboot.mail.UnitTest;
import it.ozimov.springboot.mail.logging.LoggingStrategy;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class LoggingPropertiesTest implements UnitTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldReturnDefaultEnabled() throws Exception {
        //Arrange
        LoggingProperties loggingProperties = new LoggingProperties();

        //Act
        boolean givenEnabled = loggingProperties.isEnabled();

        //Assert
        assertions.assertThat(givenEnabled).isTrue();
    }

    @Test
    public void shouldReturnDefaultLoggingStrategyWhenNotProvided() throws Exception {
        //Arrange
        LoggingProperties loggingProperties = new LoggingProperties();

        //Act
        LoggingProperties.Strategy givenLoggingStrategy = loggingProperties.getStrategy();

        //Assert
        assertions.assertThat(givenLoggingStrategy.areCustomHeadersIgnored()).isTrue();
        assertions.assertThat(givenLoggingStrategy.areNullAndEmptyCollectionsIgnored()).isTrue();
        assertions.assertThat(givenLoggingStrategy.getFrom()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getReplyTo()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getTo()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getCc()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getBcc()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getSubject()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getBody()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getAttachments()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getEncoding()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getLocale()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getSentAt()).isEqualTo(LoggingStrategy.STANDARD_DATE_FORMAT);
        assertions.assertThat(givenLoggingStrategy.getReceiptTo()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
        assertions.assertThat(givenLoggingStrategy.getDepositionNotificationTo()).isEqualTo(LoggingStrategy.PLAIN_TEXT);
    }

}