package it.ozimov.springboot.templating.mail.model.defaultimpl;

import it.ozimov.springboot.templating.mail.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.mail.internet.InternetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
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