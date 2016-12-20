package it.ozimov.springboot.templating.mail.service.defaultimpl;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class RedisBasedPersistenceServiceConstantsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public Timeout timeout = new Timeout(120, TimeUnit.SECONDS);

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void orderingKey() throws Exception {

    }

    @Test
    public void orderingKeyPrefix() throws Exception {

    }

    @Test
    public void whenOrderingKeyPrefixThenShouldNotChange() {
        //Arrange
        final String expectedPrefix = "priority-level:";

        //Act
        final String givenPrefix = RedisBasedPersistenceServiceConstants.orderingKeyPrefix();

        //Assert
        assertions.assertThat(givenPrefix)
                .describedAs("Changing constants should be avoided")
                .isEqualTo(expectedPrefix);
    }


}