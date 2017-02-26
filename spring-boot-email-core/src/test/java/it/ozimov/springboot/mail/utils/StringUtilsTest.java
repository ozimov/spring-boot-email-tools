package it.ozimov.springboot.mail.utils;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.hamcrest.Matchers.*;

public class StringUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldBeUtilityClass() throws Exception {
        //Arrange
        Constructor<?> constructor = StringUtils.class.getDeclaredConstructor();
        assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
                .as("Constructor of an Utility Class should be private")
                .isTrue();
        constructor.setAccessible(true);

        expectedException.expectCause(
                allOf(instanceOf(UnsupportedOperationException.class),
                        hasProperty("message", equalTo("This is a utility class and cannot be instantiated"))
                ));

        //Act
        constructor.newInstance();
    }

    @Test
    public void shouldEmptyStringConstantNotChange() throws Exception {
        //Act
        String constant = StringUtils.EMPTY;

        //Assert
        assertions.assertThat(constant)
                .isNotNull()
                .isEmpty();
    }

}