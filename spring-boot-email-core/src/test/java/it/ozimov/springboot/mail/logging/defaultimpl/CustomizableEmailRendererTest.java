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
import org.mockito.internal.matchers.Null;
import org.springframework.http.MediaType;

import javax.mail.internet.InternetAddress;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.fail;

public class CustomizableEmailRendererTest implements UnitTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    private DefaultEmail emailWithOnlyMandatoryFields;
    private DefaultEmail emailFullContent;

    @Before
    public void setUp() throws Exception {
        Date sentAt = Date.from(
                LocalDate.of(2014, 7, 8)
                        .atTime(4, 5, 1, 10)
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

        emailFullContent = DefaultEmail.builder()
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
    public void shouldRenderThrowExceptionOnNullEmail() throws Exception {
        //Arrange
        CustomizableEmailRenderer customizableEmailRenderer = CustomizableEmailRenderer.builder().build();

        expectedException.expect(NullPointerException.class);

        //Act
        customizableEmailRenderer.render(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithFromFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withFromFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithReplyToFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withReplyToFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithToFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withToFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithCcFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withCcFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithBccFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withBccFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithSubjectFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withSubjectFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithBodyFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withBodyFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithAttachmentsFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withAttachmentsFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithEncodingFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withEncodingFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithLocaleFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withLocaleFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithSentAtFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withSentAtFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithReceiptToFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withReceiptToFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldWithDepositionNotificationToFormatThrowExceptionOnNullOperator() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        CustomizableEmailRenderer.builder().withDepositionNotificationToFormat(null);

        //Assert
        fail();
    }

    @Test
    public void shouldBuildReturnEmptyDescriptionGivenNoCallToWitherMethods() throws Exception {
        //Arrange
        CustomizableEmailRenderer customizableEmailRenderer = CustomizableEmailRenderer.builder().build();

        //Act
        String givenPrint = customizableEmailRenderer.render(emailFullContent);

        //Assert
        assertions.assertThat(givenPrint).isEqualTo("Email{}");
    }

    @Test
    public void shouldBuildReturnFullDescriptionGivenAllCallsToWitherMethods() throws Exception {
        //Arrange
        CustomizableEmailRenderer customizableEmailRenderer = CustomizableEmailRenderer.builder()
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

        //Act
        String givenPrint = customizableEmailRenderer.render(emailFullContent);

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
        CustomizableEmailRenderer customizableEmailRenderer = CustomizableEmailRenderer.builder()
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

        //Act
        String givenPrint = customizableEmailRenderer.render(emailFullContent);

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
    public void shouldBuildIgnoreNullAndEmptyCollections() throws Exception {
        //Arrange
        CustomizableEmailRenderer customizableEmailRenderer = CustomizableEmailRenderer.builder()
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

        //Act
        String givenPrint = customizableEmailRenderer.render(emailWithOnlyMandatoryFields);

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

    @Test
    public void shouldBuildReturnNullFieldsAsNULLOrEmptyListOnProperConfigurationMethod() throws Exception {
        //Arrange
        CustomizableEmailRenderer customizableEmailRenderer = CustomizableEmailRenderer.builder()
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
                .includeNullAndEmptyCollections()
                .build();

        //Act
        String givenPrint = customizableEmailRenderer.render(emailWithOnlyMandatoryFields);

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

}