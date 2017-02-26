package it.ozimov.springboot.mail.service.exception;


import it.ozimov.springboot.mail.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;

public class CannotSendEmailExceptionTest implements UnitTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNoArgsConstructor() throws CannotSendEmailException {
        //Arrange
        expectedException.expect(CannotSendEmailException.class);

        //Act
        throw new CannotSendEmailException();
    }

    @Test
    public void testConstructorWithMessage() throws CannotSendEmailException {
        //Arrange
        final String message = "test";
        expectedException.expect(CannotSendEmailException.class);
        expectedException.expectMessage(message);

        //Act
        throw new CannotSendEmailException(message);
    }

    @Test
    public void testConstructorWithMessageAndCause() throws CannotSendEmailException {
        //Arrange
        final String message = "test";
        final Exception cause = new NullPointerException("NPE");
        expectedException.expect(CannotSendEmailException.class);
        expectedException.expectMessage(message);
        expectedException.expectCause(is(cause));

        //Act
        throw new CannotSendEmailException(message, cause);
    }

    @Test
    public void testConstructorWithCause() throws CannotSendEmailException {
        //Arrange
        final Exception cause = new NullPointerException("NPE");
        expectedException.expect(CannotSendEmailException.class);
        expectedException.expectCause(is(cause));

        //Act
        throw new CannotSendEmailException(cause);
    }

}