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

import it.ozimov.springboot.mail.logging.LoggingStrategy;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jakarta.mail.internet.InternetAddress;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static org.junit.Assert.fail;

public class EmailFieldFormatTest {

    public static final String NULL = "NULL";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldNullBeConstant() {
        //Arrange
        String expectedValue = "NULL";

        //Act
        String givenValue = EmailFieldFormat.nullValue();

        //Assert
        assertions.assertThat(givenValue).isEqualTo(expectedValue);
    }

    @Test
    public void shouldTextFormatterFromThrowExceptionGivenNullLoggingStrategy() {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        EmailFieldFormat.textFormatterFrom(null);

        //Assert
        fail();
    }

    @Test
    public void shouldTextFormatterFromReturnProperFormatter() {
        //Arrange
        final String veryLongText = "qwertyuiopasdfghjklzxcvbnm QWERTYUIOPASDFGHJKLZXCVBNM 0123456789 @#!$%^&*()_+=-?/|\"':;>.<,{[}]~`";

        UnaryOperator<String> expectedPlainTextOperator = EmailFieldFormat::plainText;
        UnaryOperator<String> expectedHiddenOperator = null;
        UnaryOperator<String> expectedFirstDozenThenStartsOperator = EmailFieldFormat::firstDozenThenStars;
        UnaryOperator<String> expectedFullTextFromCommercialAtOperator = null;
        UnaryOperator<String> expectedFullTextUpToCommercialAtOperator = null;
        UnaryOperator<String> expectedStandardDateOperator = null;
        UnaryOperator<String> expectedStandardDateWithZoneIdOperator = null;


        //Act
        UnaryOperator<String> givenPlainTextOperator = EmailFieldFormat.textFormatterFrom(LoggingStrategy.PLAIN_TEXT);
        UnaryOperator<String> givenHiddenOperator = EmailFieldFormat.textFormatterFrom(LoggingStrategy.HIDDEN);
        UnaryOperator<String> givenFirstDozenThenStartsOperator = EmailFieldFormat.textFormatterFrom(LoggingStrategy.FIRST_DOZEN_THEN_STARS);
        UnaryOperator<String> givenFullTextFromCommercialAtOperator = EmailFieldFormat.textFormatterFrom(LoggingStrategy.FULL_TEXT_FROM_COMMERCIAL_AT);
        UnaryOperator<String> givenFullTextUpToCommercialAtOperator = EmailFieldFormat.textFormatterFrom(LoggingStrategy.FULL_TEXT_UP_TO_COMMERCIAL_AT);
        UnaryOperator<String> givenStandardDateOperator = EmailFieldFormat.textFormatterFrom(LoggingStrategy.STANDARD_DATE_FORMAT);
        UnaryOperator<String> givenStandardDateWithZoneIdOperator = EmailFieldFormat.textFormatterFrom(LoggingStrategy.STANDARD_DATE_FORMAT_WITH_ZONE_ID);

        //Assert
        assertions.assertThat(givenPlainTextOperator.apply(veryLongText)).isEqualTo(expectedPlainTextOperator.apply(veryLongText));
        assertions.assertThat(givenHiddenOperator).isEqualTo(expectedHiddenOperator).isNull();
        assertions.assertThat(givenFirstDozenThenStartsOperator.apply(veryLongText)).isEqualTo(expectedFirstDozenThenStartsOperator.apply(veryLongText));
        assertions.assertThat(givenFullTextFromCommercialAtOperator).isEqualTo(expectedFullTextFromCommercialAtOperator).isNull();
        assertions.assertThat(givenFullTextUpToCommercialAtOperator).isEqualTo(expectedFullTextUpToCommercialAtOperator).isNull();
        assertions.assertThat(givenStandardDateOperator).isEqualTo(expectedStandardDateOperator).isNull();
        assertions.assertThat(givenStandardDateWithZoneIdOperator).isEqualTo(expectedStandardDateWithZoneIdOperator).isNull();
    }

    @Test
    public void shouldEmailFormatterFromThrowExceptionGivenNullLoggingStrategy() {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        EmailFieldFormat.emailFormatterFrom(null);

        //Assert
        fail();
    }

    @Test
    public void shouldEmailFormatterFromReturnProperFormatter() throws Exception {
        //Arrange
        InternetAddress internetAddress = new InternetAddress("test@email.com", "Mr. Test");

        Function<InternetAddress, String> expectedPlainTextOperator = EmailFieldFormat::plainText;
        Function<InternetAddress, String> expectedHiddenOperator = null;
        Function<InternetAddress, String> expectedFirstDozenThenStartsOperator = EmailFieldFormat::firstDozenThenStars;
        Function<InternetAddress, String> expectedFullTextFromCommercialAtOperator = EmailFieldFormat::textFromAt;
        Function<InternetAddress, String> expectedFullTextUpToCommercialAtOperator = EmailFieldFormat::textUpToAt;
        Function<InternetAddress, String> expectedStandardDateOperator = null;
        Function<InternetAddress, String> expectedStandardDateWithZoneIdOperator = null;


        //Act
        Function<InternetAddress, String> givenPlainTextOperator = EmailFieldFormat.emailFormatterFrom(LoggingStrategy.PLAIN_TEXT);
        Function<InternetAddress, String> givenHiddenOperator = EmailFieldFormat.emailFormatterFrom(LoggingStrategy.HIDDEN);
        Function<InternetAddress, String> givenFirstDozenThenStartsOperator = EmailFieldFormat.emailFormatterFrom(LoggingStrategy.FIRST_DOZEN_THEN_STARS);
        Function<InternetAddress, String> givenFullTextFromCommercialAtOperator = EmailFieldFormat.emailFormatterFrom(LoggingStrategy.FULL_TEXT_FROM_COMMERCIAL_AT);
        Function<InternetAddress, String> givenFullTextUpToCommercialAtOperator = EmailFieldFormat.emailFormatterFrom(LoggingStrategy.FULL_TEXT_UP_TO_COMMERCIAL_AT);
        Function<InternetAddress, String> givenStandardDateOperator = EmailFieldFormat.emailFormatterFrom(LoggingStrategy.STANDARD_DATE_FORMAT);
        Function<InternetAddress, String> givenStandardDateWithZoneIdOperator = EmailFieldFormat.emailFormatterFrom(LoggingStrategy.STANDARD_DATE_FORMAT_WITH_ZONE_ID);

        //Assert
        assertions.assertThat(givenPlainTextOperator.apply(internetAddress)).isEqualTo(expectedPlainTextOperator.apply(internetAddress));
        assertions.assertThat(givenHiddenOperator).isEqualTo(expectedHiddenOperator).isNull();
        assertions.assertThat(givenFirstDozenThenStartsOperator.apply(internetAddress)).isEqualTo(expectedFirstDozenThenStartsOperator.apply(internetAddress));
        assertions.assertThat(givenFullTextFromCommercialAtOperator.apply(internetAddress)).isEqualTo(expectedFullTextFromCommercialAtOperator.apply(internetAddress));
        assertions.assertThat(givenFullTextUpToCommercialAtOperator.apply(internetAddress)).isEqualTo(expectedFullTextUpToCommercialAtOperator.apply(internetAddress));
        assertions.assertThat(givenStandardDateOperator).isEqualTo(expectedStandardDateOperator).isNull();
        assertions.assertThat(givenStandardDateWithZoneIdOperator).isEqualTo(expectedStandardDateWithZoneIdOperator).isNull();
    }

    @Test
    public void shouldLocaleFormatterFromThrowExceptionGivenNullLoggingStrategy() {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        EmailFieldFormat.localeFormatterFrom(null);

        //Assert
        fail();
    }

    @Test
    public void shouldLocaleFormatterFromReturnProperFormatter() throws Exception {
        //Arrange
        Locale locale = Locale.ENGLISH;

        Function<Locale, String> expectedPlainTextOperator = EmailFieldFormat::plainText;
        Function<Locale, String> expectedHiddenOperator = null;
        Function<Locale, String> expectedFirstDozenThenStartsOperator = null;
        ;
        Function<Locale, String> expectedFullTextFromCommercialAtOperator = null;
        Function<Locale, String> expectedFullTextUpToCommercialAtOperator = null;
        Function<Locale, String> expectedStandardDateOperator = null;
        Function<Locale, String> expectedStandardDateWithZoneIdOperator = null;

        //Act
        Function<Locale, String> givenPlainTextOperator = EmailFieldFormat.localeFormatterFrom(LoggingStrategy.PLAIN_TEXT);
        Function<Locale, String> givenHiddenOperator = EmailFieldFormat.localeFormatterFrom(LoggingStrategy.HIDDEN);
        Function<Locale, String> givenFirstDozenThenStartsOperator = EmailFieldFormat.localeFormatterFrom(LoggingStrategy.FIRST_DOZEN_THEN_STARS);
        Function<Locale, String> givenFullTextFromCommercialAtOperator = EmailFieldFormat.localeFormatterFrom(LoggingStrategy.FULL_TEXT_FROM_COMMERCIAL_AT);
        Function<Locale, String> givenFullTextUpToCommercialAtOperator = EmailFieldFormat.localeFormatterFrom(LoggingStrategy.FULL_TEXT_UP_TO_COMMERCIAL_AT);
        Function<Locale, String> givenStandardDateOperator = EmailFieldFormat.localeFormatterFrom(LoggingStrategy.STANDARD_DATE_FORMAT);
        Function<Locale, String> givenStandardDateWithZoneIdOperator = EmailFieldFormat.localeFormatterFrom(LoggingStrategy.STANDARD_DATE_FORMAT_WITH_ZONE_ID);

        //Assert
        assertions.assertThat(givenPlainTextOperator.apply(locale)).isEqualTo(expectedPlainTextOperator.apply(locale));
        assertions.assertThat(givenHiddenOperator).isEqualTo(expectedHiddenOperator).isNull();
        assertions.assertThat(givenFirstDozenThenStartsOperator).isEqualTo(expectedFirstDozenThenStartsOperator).isNull();
        assertions.assertThat(givenFullTextFromCommercialAtOperator).isEqualTo(expectedFullTextFromCommercialAtOperator).isNull();
        assertions.assertThat(givenFullTextUpToCommercialAtOperator).isEqualTo(expectedFullTextUpToCommercialAtOperator).isNull();
        assertions.assertThat(givenStandardDateOperator).isEqualTo(expectedStandardDateOperator).isNull();
        assertions.assertThat(givenStandardDateWithZoneIdOperator).isEqualTo(expectedStandardDateWithZoneIdOperator).isNull();
    }

    @Test
    public void shouldDateFormatterFromThrowExceptionGivenNullLoggingStrategy() {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        EmailFieldFormat.dateFormatterFrom(null);

        //Assert
        fail();
    }

    @Test
    public void shouldDateFormatterFromReturnProperFormatter() throws Exception {
        //Arrange
        Date date = new Date();

        Function<Date, String> expectedPlainTextOperator = null;
        Function<Date, String> expectedHiddenOperator = null;
        Function<Date, String> expectedFirstDozenThenStartsOperator = null;
        ;
        Function<Date, String> expectedFullTextFromCommercialAtOperator = null;
        Function<Date, String> expectedFullTextUpToCommercialAtOperator = null;
        Function<Date, String> expectedStandardDateOperator = EmailFieldFormat::dateFormat;
        Function<Date, String> expectedStandardDateWithZoneIdOperator = EmailFieldFormat::dateFormatWithZoneId;


        //Act
        Function<Date, String> givenPlainTextOperator = EmailFieldFormat.dateFormatterFrom(LoggingStrategy.PLAIN_TEXT);
        Function<Date, String> givenHiddenOperator = EmailFieldFormat.dateFormatterFrom(LoggingStrategy.HIDDEN);
        Function<Date, String> givenFirstDozenThenStartsOperator = EmailFieldFormat.dateFormatterFrom(LoggingStrategy.FIRST_DOZEN_THEN_STARS);
        Function<Date, String> givenFullTextFromCommercialAtOperator = EmailFieldFormat.dateFormatterFrom(LoggingStrategy.FULL_TEXT_FROM_COMMERCIAL_AT);
        Function<Date, String> givenFullTextUpToCommercialAtOperator = EmailFieldFormat.dateFormatterFrom(LoggingStrategy.FULL_TEXT_UP_TO_COMMERCIAL_AT);
        Function<Date, String> givenStandardDateOperator = EmailFieldFormat.dateFormatterFrom(LoggingStrategy.STANDARD_DATE_FORMAT);
        Function<Date, String> givenStandardDateWithZoneIdOperator = EmailFieldFormat.dateFormatterFrom(LoggingStrategy.STANDARD_DATE_FORMAT_WITH_ZONE_ID);

        //Assert
        assertions.assertThat(givenPlainTextOperator).isEqualTo(expectedPlainTextOperator).isNull();
        assertions.assertThat(givenHiddenOperator).isEqualTo(expectedHiddenOperator).isNull();
        assertions.assertThat(givenFirstDozenThenStartsOperator).isEqualTo(expectedFirstDozenThenStartsOperator).isNull();
        assertions.assertThat(givenFullTextFromCommercialAtOperator).isEqualTo(expectedFullTextFromCommercialAtOperator).isNull();
        assertions.assertThat(givenFullTextUpToCommercialAtOperator).isEqualTo(expectedFullTextUpToCommercialAtOperator).isNull();
        assertions.assertThat(givenStandardDateOperator.apply(date)).isEqualTo(expectedStandardDateOperator.apply(date));
        assertions.assertThat(givenStandardDateWithZoneIdOperator.apply(date)).isEqualTo(expectedStandardDateWithZoneIdOperator.apply(date));
    }

    @Test
    public void shouldPlainTextFormatLocale() throws Exception {
        //Arrange
        Locale locale = Locale.ITALY;

        //Act
        String givenFormattedText = EmailFieldFormat.plainText(locale);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("it_IT");
    }

    @Test
    public void shouldPlainTextFormatNullLocale() throws Exception {
        //Arrange
        Locale locale = null;

        //Act
        String givenFormattedText = EmailFieldFormat.plainText(locale);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    @Test
    public void shouldPlainTextFormatInternetAddress() throws Exception {
        //Arrange
        InternetAddress internetAddress = new InternetAddress("test@email.com", "Mr. Test");

        //Act
        String givenFormattedText = EmailFieldFormat.plainText(internetAddress);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("test@email.com");
    }

    @Test
    public void shouldPlainTextFormatNullInternetAddress() throws Exception {
        //Arrange
        InternetAddress internetAddress = null;

        //Act
        String givenFormattedText = EmailFieldFormat.plainText(internetAddress);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    @Test
    public void shouldPlainTextFormatString() throws Exception {
        //Arrange
        String text = "text";

        //Act
        String givenFormattedText = EmailFieldFormat.plainText(text);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(text);
    }

    @Test
    public void shouldPlainTextFormatNullString() throws Exception {
        //Arrange
        String nullText = null;

        //Act
        String givenFormattedText = EmailFieldFormat.plainText(nullText);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    @Test
    public void shouldFirstDozenThenStarsFormatInternetAddress() throws Exception {
        //Arrange
        InternetAddress internetAddress = new InternetAddress("test@email.com", "Mr. Test");

        //Act
        String givenFormattedText = EmailFieldFormat.firstDozenThenStars(internetAddress);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("test@email.c***");
    }

    @Test
    public void shouldFirstDozenThenStarsFormatNullInternetAddress() throws Exception {
        //Arrange
        InternetAddress internetAddress = null;

        //Act
        String givenFormattedText = EmailFieldFormat.firstDozenThenStars(internetAddress);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    @Test
    public void shouldFirstDozenThenStarsFormatString() throws Exception {
        //Arrange
        String text = "12345678901234567890";

        //Act
        String givenFormattedText = EmailFieldFormat.firstDozenThenStars(text);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("123456789012***");
    }

    @Test
    public void shouldFirstDozenThenStarsFormatShortStringWithNoStars() throws Exception {
        //Arrange
        String text = "0123456789";

        //Act
        String givenFormattedText = EmailFieldFormat.firstDozenThenStars(text);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(text);
    }

    @Test
    public void shouldFirstDozenThenStarsFormatNullString() throws Exception {
        //Arrange
        String text = null;

        //Act
        String givenFormattedText = EmailFieldFormat.firstDozenThenStars(text);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    @Test
    public void shouldTextFromAtFormatInternetAddress() throws Exception {
        //Arrange
        InternetAddress internetAddress = new InternetAddress("test@email.com", "Mr. Test");

        //Act
        String givenFormattedText = EmailFieldFormat.textFromAt(internetAddress);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("***@email.com");
    }

    @Test
    public void shouldTextFromAtFormatNullInternetAddress() throws Exception {
        //Arrange
        InternetAddress internetAddress = null;

        //Act
        String givenFormattedText = EmailFieldFormat.textFromAt(internetAddress);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    @Test
    public void shouldTextFromAtFormatString() throws Exception {
        //Arrange
        String text = "test@email.com";

        //Act
        String givenFormattedText = EmailFieldFormat.textFromAt(text);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("***@email.com");
    }

    @Test
    public void shouldTextFromAtFormatNullString() throws Exception {
        //Arrange
        String text = null;

        //Act
        String givenFormattedText = EmailFieldFormat.textFromAt(text);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    @Test
    public void shouldTextFromAtFormatStringWithoutAt() throws Exception {
        //Arrange
        String text = "qwerty";

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(String.format("Given text should contain '@', while %s given.", text));

        //Act
        EmailFieldFormat.textFromAt(text);


        //Assert
        fail();
    }


    @Test
    public void shouldTextUpToAtFormatInternetAddress() throws Exception {
        //Arrange
        InternetAddress internetAddress = new InternetAddress("test@email.com", "Mr. Test");

        //Act
        String givenFormattedText = EmailFieldFormat.textUpToAt(internetAddress);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("test@***");
    }

    @Test
    public void shouldTextUpToAtFormatNullInternetAddress() throws Exception {
        //Arrange
        InternetAddress internetAddress = null;

        //Act
        String givenFormattedText = EmailFieldFormat.textUpToAt(internetAddress);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    @Test
    public void shouldTextUpToAtFormatString() throws Exception {
        //Arrange
        String text = "test@email.com";

        //Act
        String givenFormattedText = EmailFieldFormat.textUpToAt(text);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("test@***");
    }

    @Test
    public void shouldTextUpToAtFormatNullString() throws Exception {
        //Arrange
        String text = null;

        //Act
        String givenFormattedText = EmailFieldFormat.textUpToAt(text);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    @Test
    public void shouldTextUpToAtFormatStringWithoutAt() throws Exception {
        //Arrange
        String text = "qwerty";

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(String.format("Given text should contain '@', while %s given.", text));

        //Act
        EmailFieldFormat.textUpToAt(text);


        //Assert
        fail();
    }

    @Test
    public void shouldDateFormatDoFormatDate() throws Exception {
        //Arrange
        Date date = Date.from(
                LocalDate.of(2014, 7, 8)
                        .atTime(4, 5, 1, 10)
                        .atZone(ZoneId.of("UTC"))
                        .toInstant()
        );

        //Act
        String givenFormattedText = EmailFieldFormat.dateFormat(date);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("2014-07-08 04:05:01");
    }

    @Test
    public void shouldDateFormatDoFormatNullDate() throws Exception {
        //Arrange
        Date date = null;

        //Act
        String givenFormattedText = EmailFieldFormat.dateFormat(date);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    @Test
    public void shouldDateFormatWithZoneIdDoFormatDate() throws Exception {
        //Arrange
        Date date = Date.from(
                LocalDate.of(2014, 7, 8)
                        .atTime(4, 5, 1, 10)
                        .atZone(ZoneId.of("UTC"))
                        .toInstant()
        );

        //Act
        String givenFormattedText = EmailFieldFormat.dateFormatWithZoneId(date);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("2014-07-08 04:05:01 UTC");
    }

    @Test
    public void shouldDateFormatWithZoneIdDoFormatNullDate() throws Exception {
        //Arrange
        Date date = null;

        //Act
        String givenFormattedText = EmailFieldFormat.dateFormatWithZoneId(date);

        //Assert
        assertions.assertThat(givenFormattedText)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(NULL);
    }

    //TODO test new static methods

}