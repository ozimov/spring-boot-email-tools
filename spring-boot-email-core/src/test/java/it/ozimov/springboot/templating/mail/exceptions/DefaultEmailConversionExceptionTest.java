package it.ozimov.springboot.templating.mail.exceptions;

import it.ozimov.springboot.templating.mail.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;

public class DefaultEmailConversionExceptionTest implements UnitTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNoArgsConstructor() {
        expectedException.expect(EmailConversionException.class);

        //Act
        throw new EmailConversionException();
    }

    @Test
    public void testConstructorWithMessage() {
        final String message = "test";
        expectedException.expect(EmailConversionException.class);
        expectedException.expectMessage(message);

        throw new EmailConversionException(message);
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        final String message = "test";
        final Exception cause = new NullPointerException("NPE");
        expectedException.expect(EmailConversionException.class);
        expectedException.expectMessage(message);
        expectedException.expectCause(is(cause));

        throw new EmailConversionException(message, cause);
    }

    @Test
    public void testConstructorWithCause() {
        final Exception cause = new NullPointerException("NPE");
        expectedException.expect(EmailConversionException.class);
        expectedException.expectCause(is(cause));

        throw new EmailConversionException(cause);
    }

}