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

package it.ozimov.springboot.templating.mail.model.defaultimpl;

import it.ozimov.springboot.templating.mail.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.mail.internet.InternetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class DefaultEmailTest implements UnitTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEmailImplMustHaveFrom() throws Exception {
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
    public void testEmailImplMustHaveSubject() throws Exception {
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
    public void testEmailImplMustHaveBody() throws Exception {
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
    public void testEmailImplValidWithRequiredFields() throws Exception {
        //Arrange

        //Act
        final DefaultEmail email = DefaultEmail.builder()
                .from(new InternetAddress())
                .subject("subject")
                .body("body")
                .build();

        //Assert
        assertThat(email, not(is(nullValue())));
    }
}