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

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.mail.internet.InternetAddress;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class EmailFieldFormatTest {

    public static final String NULL = "NULL";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();


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