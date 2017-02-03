package it.ozimov.springboot.templating.mail.model.impl;

import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.mail.internet.InternetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

public class EmailImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEmailImplMustHaveFrom() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        EmailImpl.builder()
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
        EmailImpl.builder()
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
        EmailImpl.builder()
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
        final EmailImpl email = EmailImpl.builder()
                .from(new InternetAddress())
                .to(ImmutableList.of(new InternetAddress()))
                .subject("subject")
                .body("body")
                .build();

        //Assert
        assertThat(email, not(is(nullValue())));
    }
}