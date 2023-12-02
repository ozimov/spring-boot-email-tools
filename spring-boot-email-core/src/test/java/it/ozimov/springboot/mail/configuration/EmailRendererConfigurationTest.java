package it.ozimov.springboot.mail.configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.ozimov.springboot.mail.logging.EmailRenderer;
import it.ozimov.springboot.mail.logging.LoggingStrategy;
import it.ozimov.springboot.mail.logging.defaultimpl.CustomizableEmailRenderer;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailAttachment;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import jakarta.mail.internet.InternetAddress;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailRendererConfigurationTest {

    private static final Date SEND_AT_DATE = createSendAtDate();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Spy
    private CustomizableEmailRenderer.CustomizableEmailRendererBuilder customizableEmailRendererBuilder;

    private EmailRendererConfiguration emailRendererConfiguration;

    @Test
    public void shouldNotIgnoreWhenFormatIsNotNull() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(alwaysShowStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailFullContent();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .contains("from=from@email.com")
                .contains("replyTo=replyTo@email.com")
                .contains("to=[to_1@email.com, to_2@email.com]")
                .contains("cc=[cc_1@email.com, cc_2@email.com]")
                .contains("bcc=[bcc_1@email.com, bcc_2@email.com, bcc_3@email.com]")
                .contains("subject=subject")
                .contains("body=body")
                .contains("attachments=[attachment1.pdf, attachment2.pdf]")
                .contains("encoding=UTF-16")
                .contains("locale=it_IT")
                .contains("sentAt=2014-07-08 04:05:01")
                .contains("receiptTo=receiptTo@email.com")
                .contains("depositionNotificationTo=depositionNotificationTo@email.com}");

        verify(customizableEmailRendererBuilder).withFromFormat(any());
        verify(customizableEmailRendererBuilder).withReplyToFormat(any());
        verify(customizableEmailRendererBuilder).withToFormat(any());
        verify(customizableEmailRendererBuilder).withCcFormat(any());
        verify(customizableEmailRendererBuilder).withBccFormat(any());
        verify(customizableEmailRendererBuilder).withSubjectFormat(any());
        verify(customizableEmailRendererBuilder).withBodyFormat(any());
        verify(customizableEmailRendererBuilder).withAttachmentsFormat(any());
        verify(customizableEmailRendererBuilder).withEncodingFormat(any());
        verify(customizableEmailRendererBuilder).withLocaleFormat(any());
        verify(customizableEmailRendererBuilder).withSentAtFormat(any());
        verify(customizableEmailRendererBuilder).withReceiptToFormat(any());
        verify(customizableEmailRendererBuilder).withDepositionNotificationToFormat(any());
//        verify(customizableEmailRendererBuilder, never()).includeCustomHeaders();
//        verify(customizableEmailRendererBuilder, never()).includeNullAndEmptyCollections();

    }

    @Test
    public void shouldIgnoreWhenFormatIsNull() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(alwaysHideStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailFullContent();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent)).isEqualTo("Email{}");

        verify(customizableEmailRendererBuilder, never()).withFromFormat(any());
        verify(customizableEmailRendererBuilder, never()).withReplyToFormat(any());
        verify(customizableEmailRendererBuilder, never()).withToFormat(any());
        verify(customizableEmailRendererBuilder, never()).withCcFormat(any());
        verify(customizableEmailRendererBuilder, never()).withBccFormat(any());
        verify(customizableEmailRendererBuilder, never()).withSubjectFormat(any());
        verify(customizableEmailRendererBuilder, never()).withBodyFormat(any());
        verify(customizableEmailRendererBuilder, never()).withAttachmentsFormat(any());
        verify(customizableEmailRendererBuilder, never()).withEncodingFormat(any());
        verify(customizableEmailRendererBuilder, never()).withLocaleFormat(any());
        verify(customizableEmailRendererBuilder, never()).withSentAtFormat(any());
        verify(customizableEmailRendererBuilder, never()).withReceiptToFormat(any());
        verify(customizableEmailRendererBuilder, never()).withDepositionNotificationToFormat(any());
//        verify(customizableEmailRendererBuilder, never()).includeCustomHeaders();
//        verify(customizableEmailRendererBuilder, never()).includeNullAndEmptyCollections();
    }

    //----------------------------------------------------------------
    //---------------- NULL OR EMPTY COLLECTIONS
    //----------------------------------------------------------------

    @Test
    public void shouldNotIgnoreEmptyCollectionsWhenIgnorePropertyIsFalse() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(doNotIgnoreNullAndEmptyCollectionsStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailWithEmptyCollections();

        assertions.assertThat(emailFullContent.getTo()).isEmpty();
        assertions.assertThat(emailFullContent.getCc()).isEmpty();
        assertions.assertThat(emailFullContent.getBcc()).isEmpty();
        assertions.assertThat(emailFullContent.getAttachments()).isEmpty();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .contains("from=from@email.com")
                .contains("replyTo=replyTo@email.com")
                .contains("to=[]")
                .contains("cc=[]")
                .contains("bcc=[]")
                .contains("subject=subject")
                .contains("body=body")
                .contains("attachments=[]")
                .contains("encoding=UTF-16")
                .contains("locale=it_IT")
                .contains("sentAt=2014-07-08 04:05:01")
                .contains("receiptTo=receiptTo@email.com")
                .contains("depositionNotificationTo=depositionNotificationTo@email.com}");

        verify(customizableEmailRendererBuilder).withFromFormat(any());
        verify(customizableEmailRendererBuilder).withReplyToFormat(any());
        verify(customizableEmailRendererBuilder).withToFormat(any());
        verify(customizableEmailRendererBuilder).withCcFormat(any());
        verify(customizableEmailRendererBuilder).withBccFormat(any());
        verify(customizableEmailRendererBuilder).withSubjectFormat(any());
        verify(customizableEmailRendererBuilder).withBodyFormat(any());
        verify(customizableEmailRendererBuilder).withAttachmentsFormat(any());
        verify(customizableEmailRendererBuilder).withEncodingFormat(any());
        verify(customizableEmailRendererBuilder).withLocaleFormat(any());
        verify(customizableEmailRendererBuilder).withSentAtFormat(any());
        verify(customizableEmailRendererBuilder).withReceiptToFormat(any());
        verify(customizableEmailRendererBuilder).withDepositionNotificationToFormat(any());
        verify(customizableEmailRendererBuilder).includeNullAndEmptyCollections();
    }

    @Test
    public void shouldNotIgnoreNullCollectionsWhenIgnorePropertyIsFalse() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(doNotIgnoreNullAndEmptyCollectionsStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailWithNullCollections();

        assertions.assertThat(emailFullContent.getTo()).isNull();
        assertions.assertThat(emailFullContent.getCc()).isNull();
        assertions.assertThat(emailFullContent.getBcc()).isNull();
        assertions.assertThat(emailFullContent.getAttachments()).isEmpty();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .contains("from=from@email.com")
                .contains("replyTo=replyTo@email.com")
                .contains("to=NULL")
                .contains("cc=NULL")
                .contains("bcc=NULL")
                .contains("subject=subject")
                .contains("body=body")
                .contains("attachments=[]")
                .contains("encoding=UTF-16")
                .contains("locale=it_IT")
                .contains("sentAt=2014-07-08 04:05:01")
                .contains("receiptTo=receiptTo@email.com")
                .contains("depositionNotificationTo=depositionNotificationTo@email.com}");

        verify(customizableEmailRendererBuilder).withFromFormat(any());
        verify(customizableEmailRendererBuilder).withReplyToFormat(any());
        verify(customizableEmailRendererBuilder).withToFormat(any());
        verify(customizableEmailRendererBuilder).withCcFormat(any());
        verify(customizableEmailRendererBuilder).withBccFormat(any());
        verify(customizableEmailRendererBuilder).withSubjectFormat(any());
        verify(customizableEmailRendererBuilder).withBodyFormat(any());
        verify(customizableEmailRendererBuilder).withAttachmentsFormat(any());
        verify(customizableEmailRendererBuilder).withEncodingFormat(any());
        verify(customizableEmailRendererBuilder).withLocaleFormat(any());
        verify(customizableEmailRendererBuilder).withSentAtFormat(any());
        verify(customizableEmailRendererBuilder).withReceiptToFormat(any());
        verify(customizableEmailRendererBuilder).withDepositionNotificationToFormat(any());
        verify(customizableEmailRendererBuilder).includeNullAndEmptyCollections();
    }

    @Test
    public void shouldIgnoreEmptyCollectionsWhenIgnorePropertyIsTrue() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(ignoreNullAndEmptyCollectionsStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailWithEmptyCollections();

        assertions.assertThat(emailFullContent.getTo()).isEmpty();
        assertions.assertThat(emailFullContent.getCc()).isEmpty();
        assertions.assertThat(emailFullContent.getBcc()).isEmpty();
        assertions.assertThat(emailFullContent.getAttachments()).isEmpty();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .contains("from=from@email.com")
                .contains("replyTo=replyTo@email.com")
                .doesNotContain("to=")
                .doesNotContain("cc=")
                .doesNotContain("bcc=")
                .contains("subject=subject")
                .contains("body=body")
                .doesNotContain("attachments=")
                .contains("encoding=UTF-16")
                .contains("locale=it_IT")
                .contains("sentAt=2014-07-08 04:05:01")
                .contains("receiptTo=receiptTo@email.com")
                .contains("depositionNotificationTo=depositionNotificationTo@email.com}");

        verify(customizableEmailRendererBuilder).withFromFormat(any());
        verify(customizableEmailRendererBuilder).withReplyToFormat(any());
        verify(customizableEmailRendererBuilder).withToFormat(any());
        verify(customizableEmailRendererBuilder).withCcFormat(any());
        verify(customizableEmailRendererBuilder).withBccFormat(any());
        verify(customizableEmailRendererBuilder).withSubjectFormat(any());
        verify(customizableEmailRendererBuilder).withBodyFormat(any());
        verify(customizableEmailRendererBuilder).withAttachmentsFormat(any());
        verify(customizableEmailRendererBuilder).withEncodingFormat(any());
        verify(customizableEmailRendererBuilder).withLocaleFormat(any());
        verify(customizableEmailRendererBuilder).withSentAtFormat(any());
        verify(customizableEmailRendererBuilder).withReceiptToFormat(any());
        verify(customizableEmailRendererBuilder).withDepositionNotificationToFormat(any());
        verify(customizableEmailRendererBuilder, never()).includeNullAndEmptyCollections();
    }

    //----------------------------------------------------------------
    //---------------- CUSTOM HEADERS
    //----------------------------------------------------------------

    @Test
    public void shouldNotIgnoreCustomHeadersWhenIgnorePropertyIsFalseAndCollectionsCouldBeNonEmptyOrNonNull() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(doNotIgnoreCustomHeadersEvenIfNullOrEmptyStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailFullContent();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .contains("customHeaders=[header1=value1, header2=value2]");

        verify(customizableEmailRendererBuilder).includeCustomHeaders();
    }

    @Test
    public void shouldNotIgnoreEmptyCustomHeadersWhenIgnorePropertyIsFalseAndCollectionsCouldBeNonEmptyOrNonNull() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(doNotIgnoreCustomHeadersEvenIfNullOrEmptyStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailWithEmptyCustomHeaders();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .contains("customHeaders=[]");

        verify(customizableEmailRendererBuilder).includeCustomHeaders();
    }

    @Test
    public void shouldNotIgnoreNullCustomHeadersWhenIgnorePropertyIsFalseAndCollectionsCouldBeNonEmptyOrNonNull() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(doNotIgnoreCustomHeadersEvenIfNullOrEmptyStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailWithNullCustomHeaders();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .contains("customHeaders=NULL");

        verify(customizableEmailRendererBuilder).includeCustomHeaders();
    }

    @Test
    public void shouldNotIgnoreCustomHeadersWhenIgnorePropertyIsFalseAndCollectionsMustBeNonEmptyOrNonNull() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(doNotIgnoreCustomHeadersIfNotNullOrEmptyStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailFullContent();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .contains("customHeaders=[header1=value1, header2=value2]");

        verify(customizableEmailRendererBuilder).includeCustomHeaders();
    }

    @Test
    public void shouldIgnoreEmptyCustomHeadersWhenIgnorePropertyIsFalseButCollectionsMustBeNonEmptyOrNonNull() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(doNotIgnoreCustomHeadersIfNotNullOrEmptyStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailWithEmptyCustomHeaders();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .doesNotContain("customHeaders=");

        verify(customizableEmailRendererBuilder).includeCustomHeaders();
    }

    @Test
    public void shoulIgnoreNullCustomHeadersWhenIgnorePropertyIsFalseButCollectionsMustBeNonEmptyOrNonNull() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(doNotIgnoreCustomHeadersIfNotNullOrEmptyStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailWithNullCustomHeaders();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .doesNotContain("customHeaders=");

        verify(customizableEmailRendererBuilder).includeCustomHeaders();
    }

    @Test
    public void shouldIgnoreCustomHeadersWhenIgnorePropertyIsTrueEvenIfCollectionsCanBeEmptyOrNull() throws Exception {
        //Arrange
        final LoggingProperties loggingProperties = new LoggingProperties();
        loggingProperties.setStrategy(ignoreCustomHeadersStrategy());
        emailRendererConfiguration = createEmailRendererConfigurationWithStrategy(loggingProperties);

        Email emailFullContent = createEmailFullContent();

        //Act
        EmailRenderer emailRenderer = emailRendererConfiguration.emailRenderer();

        //Assert
        assertions.assertThat(emailRenderer.render(emailFullContent))
                .startsWith("Email{").endsWith("}")
                .doesNotContain("customHeaders=");

        verify(customizableEmailRendererBuilder, never()).includeCustomHeaders();
    }

    private EmailRendererConfiguration createEmailRendererConfigurationWithStrategy(final LoggingProperties loggingProperties) {
        final EmailRendererConfiguration emailRendererConfiguration = spy(new EmailRendererConfiguration(loggingProperties));
        when(emailRendererConfiguration.createCustomizableEmailRendererBuilder()).thenReturn(customizableEmailRendererBuilder);
        return emailRendererConfiguration;
    }

    private LoggingProperties.Strategy alwaysHideStrategy() {
        LoggingProperties.Strategy strategy = new LoggingProperties.Strategy();

        strategy.setFrom(LoggingStrategy.HIDDEN);
        strategy.setReplyTo(LoggingStrategy.HIDDEN);
        strategy.setTo(LoggingStrategy.HIDDEN);
        strategy.setCc(LoggingStrategy.HIDDEN);
        strategy.setBcc(LoggingStrategy.HIDDEN);
        strategy.setSubject(LoggingStrategy.HIDDEN);
        strategy.setBody(LoggingStrategy.HIDDEN);
        strategy.setAttachments(LoggingStrategy.HIDDEN);
        strategy.setEncoding(LoggingStrategy.HIDDEN);
        strategy.setLocale(LoggingStrategy.HIDDEN);
        strategy.setSentAt(LoggingStrategy.HIDDEN);
        strategy.setReceiptTo(LoggingStrategy.HIDDEN);
        strategy.setDepositionNotificationTo(LoggingStrategy.HIDDEN);


        return strategy;
    }

    private LoggingProperties.Strategy alwaysShowStrategy() {
        LoggingProperties.Strategy strategy = new LoggingProperties.Strategy();

        strategy.setFrom(LoggingStrategy.PLAIN_TEXT);
        strategy.setReplyTo(LoggingStrategy.PLAIN_TEXT);
        strategy.setTo(LoggingStrategy.PLAIN_TEXT);
        strategy.setCc(LoggingStrategy.PLAIN_TEXT);
        strategy.setBcc(LoggingStrategy.PLAIN_TEXT);
        strategy.setSubject(LoggingStrategy.PLAIN_TEXT);
        strategy.setBody(LoggingStrategy.PLAIN_TEXT);
        strategy.setAttachments(LoggingStrategy.PLAIN_TEXT);
        strategy.setEncoding(LoggingStrategy.PLAIN_TEXT);
        strategy.setLocale(LoggingStrategy.PLAIN_TEXT);
        strategy.setSentAt(LoggingStrategy.STANDARD_DATE_FORMAT);
        strategy.setReceiptTo(LoggingStrategy.PLAIN_TEXT);
        strategy.setDepositionNotificationTo(LoggingStrategy.PLAIN_TEXT);

        return strategy;
    }

    private LoggingProperties.Strategy ignoreNullAndEmptyCollectionsStrategy() {
        LoggingProperties.Strategy strategy = new LoggingProperties.Strategy();

        LoggingProperties.Strategy.Ignore ignore = new LoggingProperties.Strategy.Ignore();
        ignore.setNullAndEmptyCollections(true);
        strategy.setIgnore(ignore);

        return strategy;
    }

    private LoggingProperties.Strategy doNotIgnoreNullAndEmptyCollectionsStrategy() {
        LoggingProperties.Strategy strategy = new LoggingProperties.Strategy();

        LoggingProperties.Strategy.Ignore ignore = new LoggingProperties.Strategy.Ignore();
        ignore.setNullAndEmptyCollections(false);
        strategy.setIgnore(ignore);

        return strategy;
    }

    private LoggingProperties.Strategy ignoreCustomHeadersStrategy() {
        LoggingProperties.Strategy strategy = new LoggingProperties.Strategy();

        LoggingProperties.Strategy.Ignore ignore = new LoggingProperties.Strategy.Ignore();
        ignore.setNullAndEmptyCollections(false);
        ignore.setCustomHeaders(true);
        strategy.setIgnore(ignore);

        return strategy;
    }

    private LoggingProperties.Strategy doNotIgnoreCustomHeadersEvenIfNullOrEmptyStrategy() {
        LoggingProperties.Strategy strategy = new LoggingProperties.Strategy();

        LoggingProperties.Strategy.Ignore ignore = new LoggingProperties.Strategy.Ignore();
        ignore.setNullAndEmptyCollections(false);
        ignore.setCustomHeaders(false);
        strategy.setIgnore(ignore);

        return strategy;
    }

    private LoggingProperties.Strategy doNotIgnoreCustomHeadersIfNotNullOrEmptyStrategy() {
        LoggingProperties.Strategy strategy = new LoggingProperties.Strategy();

        LoggingProperties.Strategy.Ignore ignore = new LoggingProperties.Strategy.Ignore();
        ignore.setNullAndEmptyCollections(true);
        ignore.setCustomHeaders(false);
        strategy.setIgnore(ignore);

        return strategy;
    }


    private static Date createSendAtDate() {
        return Date.from(
                LocalDate.of(2014, 7, 8)
                        .atTime(4, 5, 1, 10)
                        .atZone(ZoneId.of("UTC"))
                        .toInstant()
        );
    }

    private Email createEmailFullContent() throws Exception {
        return DefaultEmail.builder()
                .from(new InternetAddress("from@email.com"))
                .to(ImmutableList.of(new InternetAddress("to_1@email.com"),
                        new InternetAddress("to_2@email.com")))
                .cc(ImmutableList.of(new InternetAddress("cc_1@email.com"),
                        new InternetAddress("cc_2@email.com")))
                .bcc(ImmutableList.of(new InternetAddress("bcc_1@email.com"),
                        new InternetAddress("bcc_2@email.com"),
                        new InternetAddress("bcc_3@email.com")))
                .replyTo(new InternetAddress("replyTo@email.com"))
                .receiptTo(new InternetAddress("receiptTo@email.com"))
                .depositionNotificationTo(new InternetAddress("depositionNotificationTo@email.com"))
                .encoding("UTF-16")
                .locale(Locale.ITALY)
                .sentAt(SEND_AT_DATE)
                .subject("subject")
                .body("body")
                .attachment(DefaultEmailAttachment.builder().attachmentName("attachment1.pdf").mediaType(MediaType.APPLICATION_PDF).attachmentData(new byte[]{}).build())
                .attachment(DefaultEmailAttachment.builder().attachmentName("attachment2.pdf").mediaType(MediaType.APPLICATION_PDF).attachmentData(new byte[]{}).build())
                .customHeaders(ImmutableMap.of("header1", "value1", "header2", "value2"))
                .build();
    }

    private Email createEmailWithEmptyCollections() throws Exception {
        return DefaultEmail.builder()
                .from(new InternetAddress("from@email.com"))
                .to(ImmutableList.of())
                .cc(ImmutableList.of())
                .bcc(ImmutableList.of())
                .replyTo(new InternetAddress("replyTo@email.com"))
                .receiptTo(new InternetAddress("receiptTo@email.com"))
                .depositionNotificationTo(new InternetAddress("depositionNotificationTo@email.com"))
                .encoding("UTF-16")
                .locale(Locale.ITALY)
                .sentAt(SEND_AT_DATE)
                .subject("subject")
                .body("body")
                .customHeaders(ImmutableMap.of("header1", "value1", "header2", "value2"))
                .build();
    }

    private Email createEmailWithNullCollections() throws Exception {
        return DefaultEmail.builder()
                .from(new InternetAddress("from@email.com"))
                .to(null)
                .cc(null)
                .bcc(null)
                .replyTo(new InternetAddress("replyTo@email.com"))
                .receiptTo(new InternetAddress("receiptTo@email.com"))
                .depositionNotificationTo(new InternetAddress("depositionNotificationTo@email.com"))
                .encoding("UTF-16")
                .locale(Locale.ITALY)
                .sentAt(SEND_AT_DATE)
                .subject("subject")
                .body("body")
                .customHeaders(ImmutableMap.of("header1", "value1", "header2", "value2"))
                .build();
    }

    private Email createEmailWithEmptyCustomHeaders() throws Exception {
        return DefaultEmail.builder()
                .from(new InternetAddress("from@email.com"))
                .to(null)
                .cc(null)
                .bcc(null)
                .replyTo(new InternetAddress("replyTo@email.com"))
                .receiptTo(new InternetAddress("receiptTo@email.com"))
                .depositionNotificationTo(new InternetAddress("depositionNotificationTo@email.com"))
                .encoding("UTF-16")
                .locale(Locale.ITALY)
                .sentAt(SEND_AT_DATE)
                .subject("subject")
                .body("body")
                .customHeaders(ImmutableMap.of())
                .build();
    }

    private Email createEmailWithNullCustomHeaders() throws Exception {
        return DefaultEmail.builder()
                .from(new InternetAddress("from@email.com"))
                .to(null)
                .cc(null)
                .bcc(null)
                .replyTo(new InternetAddress("replyTo@email.com"))
                .receiptTo(new InternetAddress("receiptTo@email.com"))
                .depositionNotificationTo(new InternetAddress("depositionNotificationTo@email.com"))
                .encoding("UTF-16")
                .locale(Locale.ITALY)
                .sentAt(SEND_AT_DATE)
                .subject("subject")
                .body("body")
                .customHeaders(null)
                .build();
    }

}