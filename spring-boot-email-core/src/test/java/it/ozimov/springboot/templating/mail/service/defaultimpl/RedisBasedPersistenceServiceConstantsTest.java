package it.ozimov.springboot.templating.mail.service.defaultimpl;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class RedisBasedPersistenceServiceConstantsTest {

    private static final String EXPECTED_ORDERING_KEY_PREFIX = "priority-level:";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldThrowExceptionWhenOrderingKeyIsConstructedFromNegativeNumber() throws Exception {
        //Arrange
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Priority level must be a positive integer number");

        //Act
        RedisBasedPersistenceServiceConstants.orderingKey(-1);
    }

    @Test
    public void shouldCreateOrderingKeyPrefixFromZero() throws Exception {
        //Arrange
        int priorityLevel = 0;
        String expectedOrderingKey = EXPECTED_ORDERING_KEY_PREFIX + priorityLevel;

        //Act
        String givenOrderingKey = RedisBasedPersistenceServiceConstants.orderingKey(priorityLevel);

        //Assert
        assertions.assertThat(givenOrderingKey).isEqualTo(expectedOrderingKey);
    }

    @Test
    public void shouldCreateOrderingKeyPrefixFromPositiveNumber() throws Exception {
        //Arrange
        int priorityLevel = 10;
        String expectedOrderingKey = EXPECTED_ORDERING_KEY_PREFIX + priorityLevel;

        //Act
        String givenOrderingKey = RedisBasedPersistenceServiceConstants.orderingKey(priorityLevel);

        //Assert
        assertions.assertThat(givenOrderingKey).isEqualTo(expectedOrderingKey);
    }

    @Test
    public void shouldOrderingKeyPrefixNotChange() {
        //Act
        final String givenPrefix = RedisBasedPersistenceServiceConstants.orderingKeyPrefix();

        //Assert
        assertions.assertThat(givenPrefix)
                .describedAs("Changing constants should be worth prison for life")
                .isEqualTo(EXPECTED_ORDERING_KEY_PREFIX);
    }

}