package it.ozimov.springboot.mail.service.exception;


import it.ozimov.springboot.mail.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;

public class TemplateExceptionTest implements UnitTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNoArgsConstructor() throws TemplateException {
        //Arrange
        expectedException.expect(TemplateException.class);

        //Act
        throw new TemplateException();
    }

    @Test
    public void testConstructorWithMessage() throws TemplateException {
        //Arrange
        final String message = "test";
        expectedException.expect(TemplateException.class);
        expectedException.expectMessage(message);

        //Act
        throw new TemplateException(message);
    }

    @Test
    public void testConstructorWithMessageAndCause() throws TemplateException {
        //Arrange
        final String message = "test";
        final Exception cause = new NullPointerException("NPE");
        expectedException.expect(TemplateException.class);
        expectedException.expectMessage(message);
        expectedException.expectCause(is(cause));

        //Act
        throw new TemplateException(message, cause);
    }

    @Test
    public void testConstructorWithCause() throws TemplateException {
        //Arrange
        final Exception cause = new NullPointerException("NPE");
        expectedException.expect(TemplateException.class);
        expectedException.expectCause(is(cause));

        //Act
        throw new TemplateException(cause);
    }

}