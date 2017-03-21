package it.ozimov.springboot.mail.logging.defaultimpl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.ozimov.springboot.mail.UnitTest;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailAttachment;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.MediaType;

import javax.mail.internet.InternetAddress;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;

public class EmailRendererBuilderTest implements UnitTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    private DefaultEmail emailWithOnlyMandatoryFields;
    private DefaultEmail emailFullShortContent;

    @Before
    public void setUp() throws Exception {
        Date sentAt = Date.from(
                LocalDate.of(2014, 7, 8)
                        .atTime(4,5, 1, 10)
                        .atZone(ZoneId.of("UTC"))
                        .toInstant()
        );

        emailWithOnlyMandatoryFields = DefaultEmail.builder()
                .from(new InternetAddress("from@email.com"))
                .to(ImmutableList.of(new InternetAddress("to_1@email.com"),
                        new InternetAddress("to_2@email.com")))
                .subject("subject")
                .body("body")
                .build();

        emailFullShortContent = DefaultEmail.builder()
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
                .sentAt(sentAt)
                .subject("subject")
                .body("body")
                .attachment(DefaultEmailAttachment.builder().attachmentName("attachment1.pdf").mediaType(MediaType.APPLICATION_PDF).attachmentData(new byte[]{}).build())
                .attachment(DefaultEmailAttachment.builder().attachmentName("attachment2.pdf").mediaType(MediaType.APPLICATION_PDF).attachmentData(new byte[]{}).build())
                .customHeaders(ImmutableMap.of("header1", "value1", "header2", "value2"))
                .build();
    }

    @Test
    public void shouldBuildReturnEmptyDescriptionGivenNoCallToWitherMethods() throws Exception {
        //Arrange

        //Act
        String givenPrint = EmailRendererBuilder.builderFor(emailFullShortContent).build();

        //Assert
        assertions.assertThat(givenPrint).isEqualTo("Email{}");
    }

    @Test
    public void shouldBuildReturnFullDescriptionGivenAllCallsToWitherMethods() throws Exception {
        //Arrange

        //Act
        String givenPrint = EmailRendererBuilder.builderFor(emailFullShortContent)
                .withFromFormat(EmailFieldFormat::plainText)
                .withToFormat(EmailFieldFormat::plainText)
                .withCcFormat(EmailFieldFormat::plainText)
                .withBccFormat(EmailFieldFormat::plainText)
                .withReplyToFormat(EmailFieldFormat::plainText)
                .withSubjectFormat(EmailFieldFormat::plainText)
                .withBodyFormat(EmailFieldFormat::plainText)
                .withAttachmentsFormat(EmailFieldFormat::plainText)
                .withEncodingFormat(EmailFieldFormat::plainText)
                .withLocaleFormat(EmailFieldFormat::plainText)
                .withSentAtFormat(EmailFieldFormat::dateFormat)
                .withReceiptToFormat(EmailFieldFormat::plainText)
                .withDepositionNotificationToFormat(EmailFieldFormat::plainText)
                .includeCustomHeaders()
                .build();

        //Assert
        assertions.assertThat(givenPrint)
                .startsWith("Email{")
                .contains("from=from@email.com")
                .contains(", replyTo=replyTo@email.com")
                .contains(", to=[to_1@email.com, to_2@email.com]")
                .contains(", cc=[cc_1@email.com, cc_2@email.com]")
                .contains(", bcc=[bcc_1@email.com, bcc_2@email.com, bcc_3@email.com]")
                .contains(", subject=subject")
                .contains(", body=body")
                .contains(", attachments=[attachment1.pdf, attachment2.pdf]")
                .contains(", encoding=UTF-16")
                .contains(", locale=it_IT")
                .contains(", sentAt=2014-07-08 04:05:01")
                .contains(", receiptTo=receiptTo@email.com")
                .contains(", depositionNotificationTo=depositionNotificationTo@email.com")
                .contains(", customHeaders=[header1=value1, header2=value2]")
                .endsWith("}");
    }


    @Test
    public void shouldBuildReturnFullDescriptionGivenSpecialEmailFieldFormats() throws Exception {
        //Arrange

        //Act
        String givenPrint = EmailRendererBuilder.builderFor(emailFullShortContent)
                .withFromFormat(EmailFieldFormat::textFromAt)
                .withToFormat(EmailFieldFormat::textUpToAt)
                .withCcFormat(EmailFieldFormat::textFromAt)
                .withBccFormat(EmailFieldFormat::textUpToAt)
                .withReplyToFormat(EmailFieldFormat::textFromAt)
                .withSubjectFormat(EmailFieldFormat::plainText)
                .withBodyFormat(EmailFieldFormat::plainText)
                .withAttachmentsFormat(EmailFieldFormat::plainText)
                .withEncodingFormat(EmailFieldFormat::plainText)
                .withLocaleFormat(EmailFieldFormat::plainText)
                .withSentAtFormat(EmailFieldFormat::dateFormatWithZoneId)
                .withReceiptToFormat(EmailFieldFormat::textFromAt)
                .withDepositionNotificationToFormat(EmailFieldFormat::textUpToAt)
                .includeCustomHeaders()
                .build();

        //Assert
        assertions.assertThat(givenPrint)
                .startsWith("Email{")
                .contains("from=***@email.com")
                .contains(", replyTo=***@email.com")
                .contains(", to=[to_1@***, to_2@***]")
                .contains(", cc=[***@email.com, ***@email.com]")
                .contains(", bcc=[bcc_1@***, bcc_2@***, bcc_3@***]")
                .contains(", subject=subject")
                .contains(", body=body")
                .contains(", attachments=[attachment1.pdf, attachment2.pdf]")
                .contains(", encoding=UTF-16")
                .contains(", locale=it_IT")
                .contains(", sentAt=2014-07-08 04:05:01 UTC")
                .contains(", receiptTo=***@email.com")
                .contains(", depositionNotificationTo=depositionNotificationTo@***")
                .contains(", customHeaders=[header1=value1, header2=value2]")
                .endsWith("}");
    }

    @Test
    public void shouldBuildReturnNullFieldsAsNULLOrEmptyList() throws Exception {
        //Arrange

        //Act
        String givenPrint = EmailRendererBuilder.builderFor(emailWithOnlyMandatoryFields)
                .withFromFormat(EmailFieldFormat::plainText)
                .withToFormat(EmailFieldFormat::plainText)
                .withCcFormat(EmailFieldFormat::plainText)
                .withBccFormat(EmailFieldFormat::plainText)
                .withReplyToFormat(EmailFieldFormat::plainText)
                .withSubjectFormat(EmailFieldFormat::plainText)
                .withBodyFormat(EmailFieldFormat::plainText)
                .withAttachmentsFormat(EmailFieldFormat::plainText)
                .withEncodingFormat(EmailFieldFormat::plainText)
                .withLocaleFormat(EmailFieldFormat::plainText)
                .withSentAtFormat(EmailFieldFormat::dateFormat)
                .withReceiptToFormat(EmailFieldFormat::plainText)
                .withDepositionNotificationToFormat(EmailFieldFormat::plainText)
                .includeCustomHeaders()
                .build();

        //Assert
        assertions.assertThat(givenPrint)
                .startsWith("Email{")
                .contains("from=from@email.com")
                .contains(", replyTo=NULL")
                .contains(", to=[to_1@email.com, to_2@email.com]")
                .contains(", cc=[]")
                .contains(", bcc=[]")
                .contains(", subject=subject")
                .contains(", body=body")
                .contains(", attachments=[]")
                .contains(", encoding=UTF-8")
                .contains(", locale=NULL")
                .contains(", sentAt=NULL")
                .contains(", receiptTo=NULL")
                .contains(", depositionNotificationTo=NULL")
                .contains(", customHeaders=[]")
                .endsWith("}");
    }

    @Test
    public void shouldBuildIgnoreNullAndEmptyCollectionsOnProperConfigurationMethod() throws Exception {
        //Arrange

        //Act
        String givenPrint = EmailRendererBuilder.builderFor(emailWithOnlyMandatoryFields)
                .withFromFormat(EmailFieldFormat::plainText)
                .withToFormat(EmailFieldFormat::plainText)
                .withCcFormat(EmailFieldFormat::plainText)
                .withBccFormat(EmailFieldFormat::plainText)
                .withReplyToFormat(EmailFieldFormat::plainText)
                .withSubjectFormat(EmailFieldFormat::plainText)
                .withBodyFormat(EmailFieldFormat::plainText)
                .withAttachmentsFormat(EmailFieldFormat::plainText)
                .withEncodingFormat(EmailFieldFormat::plainText)
                .withLocaleFormat(EmailFieldFormat::plainText)
                .withSentAtFormat(EmailFieldFormat::dateFormat)
                .withReceiptToFormat(EmailFieldFormat::plainText)
                .withDepositionNotificationToFormat(EmailFieldFormat::plainText)
                .includeCustomHeaders()
                .ignoreNullAndEmptyCollections()
                .build();

        //Assert
        assertions.assertThat(givenPrint)
                .startsWith("Email{")
                .contains("from=from@email.com")
                .doesNotContain(", replyTo=NULL")
                .contains(", to=[to_1@email.com, to_2@email.com]")
                .doesNotContain(", cc=[]")
                .doesNotContain(", bcc=[]")
                .contains(", subject=subject")
                .contains(", body=body")
                .doesNotContain(", attachments=[]")
                .contains(", encoding=UTF-8")
                .doesNotContain(", locale=NULL")
                .doesNotContain(", sentAt=NULL")
                .doesNotContain(", receiptTo=NULL")
                .doesNotContain(", depositionNotificationTo=NULL")
                .doesNotContain(", customHeaders=[]")
                .endsWith("}");
    }

}