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

package it.ozimov.springboot.mail.service.exception;

import it.ozimov.springboot.mail.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;

public class CannotDeserializeExceptionTest implements UnitTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNoArgsConstructor() {
        //Arrange
        expectedException.expect(CannotDeserializeException.class);

        //Act
        throw new CannotDeserializeException();
    }

    @Test
    public void testConstructorWithMessage() {
        //Arrange
        final String message = "test";
        expectedException.expect(CannotDeserializeException.class);
        expectedException.expectMessage(message);

        //Act
        throw new CannotDeserializeException(message);
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        //Arrange
        final String message = "test";
        final Exception cause = new NullPointerException("NPE");
        expectedException.expect(CannotDeserializeException.class);
        expectedException.expectMessage(message);
        expectedException.expectCause(is(cause));

        //Act
        throw new CannotDeserializeException(message, cause);
    }

    @Test
    public void testConstructorWithCause() {
        //Arrange
        final Exception cause = new NullPointerException("NPE");
        expectedException.expect(CannotDeserializeException.class);
        expectedException.expectCause(is(cause));

        //Act
        throw new CannotDeserializeException(cause);
    }

}