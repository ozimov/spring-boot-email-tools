package it.ozimov.springboot.templating.mail.utils;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class StringUtilsTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

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