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

package it.ozimov.springboot.mail.model.defaultimpl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.ozimov.springboot.mail.UnitTest;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jakarta.mail.internet.InternetAddress;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class DefaultEmailTest implements UnitTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void testDefaultEmaillMustHaveFrom() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        DefaultEmail.builder()
                .subject("subject")
                .body("body")
                .build();

        //Assert
        fail("Expected a NullPointerException for missing mandatory field");
    }

    @Test
    public void testDefaultEmaillMustHaveSubject() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        DefaultEmail.builder()
                .from(new InternetAddress())
                .body("body")
                .build();

        //Assert
        fail("Expected a NullPointerException for missing mandatory field");
    }

    @Test
    public void testDefaultEmaillMustHaveBody() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        DefaultEmail.builder()
                .from(new InternetAddress())
                .subject("subject")
                .build();

        //Assert
        fail("Expected a NullPointerException for missing mandatory field");
    }

    @Test
    public void testDefaultEmaillValidWithRequiredFields() throws Exception {
        //Arrange

        //Act
        final DefaultEmail email = DefaultEmail.builder()
                .from(new InternetAddress())
                .to(ImmutableList.of(new InternetAddress()))
                .subject("subject")
                .body("body")
                .build();

        //Assert
        assertThat(email, not(is(nullValue())));
    }

    @Test
    public void shouldDefaultEmailHaveDefaultEmptyCustomHeaders() throws Exception {
        //Arrange
        final DefaultEmail email = DefaultEmail.builder()
                .from(new InternetAddress())
                .to(ImmutableList.of(new InternetAddress()))
                .subject("subject")
                .body("body")
                .build();

        //Act
        final Map<String, String> givenCustomHeaders = email.getCustomHeaders();

        //Assert
        assertions.assertThat(givenCustomHeaders).isNotNull().isEmpty();
    }

    @Test
    public void shouldDefaultEmailReturnGivenCustomHeaders() throws Exception {
        //Arrange
        final Map<String, String> expectedCustomHeaders = ImmutableMap.of("K1", "V1", "K2", "V2");

        final DefaultEmail email = DefaultEmail.builder()
                .from(new InternetAddress())
                .to(ImmutableList.of(new InternetAddress()))
                .subject("subject")
                .body("body")
                .customHeaders(expectedCustomHeaders)
                .build();

        //Act
        final Map<String, String> givenCustomHeaders = email.getCustomHeaders();

        //Assert
        assertions.assertThat(givenCustomHeaders).isNotNull().isEqualTo(expectedCustomHeaders);
    }

}