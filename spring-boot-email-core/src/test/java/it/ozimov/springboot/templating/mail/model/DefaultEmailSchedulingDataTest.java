package it.ozimov.springboot.templating.mail.model;

import it.ozimov.springboot.templating.mail.UnitTest;
import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.templating.mail.utils.TimeUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.time.OffsetDateTime;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.getSimpleMail;
import static org.hamcrest.CoreMatchers.is;

public class DefaultEmailSchedulingDataTest implements UnitTest {

    @Rule
    public JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void testCompareTo() throws Exception {
        //Arrange
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();
        final DefaultEmailSchedulingData reference = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(1)
                .desiredPriority(1)
                .build();
        final DefaultEmailSchedulingData smallerPrio = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(2)
                .desiredPriority(2)
                .build();
        final DefaultEmailSchedulingData before = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime.minusDays(1))
                .assignedPriority(1)
                .desiredPriority(1)
                .build();
        final DefaultEmailSchedulingData after = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime.plusDays(1))
                .assignedPriority(1)
                .desiredPriority(1)
                .build();

        //Act+Assert
        assertions.assertThat(reference).isLessThan(smallerPrio);
        assertions.assertThat(smallerPrio).isGreaterThan(reference);

        assertions.assertThat(reference).isGreaterThan(before);
        assertions.assertThat(before).isLessThan(reference);

        assertions.assertThat(reference).isLessThan(after);
        assertions.assertThat(after).isGreaterThan(reference);

        assertions.assertThat(smallerPrio).isLessThan(after);
        assertions.assertThat(after).isGreaterThan(smallerPrio);

        assertions.assertThat(smallerPrio).isGreaterThan(before);
        assertions.assertThat(before).isLessThan(smallerPrio);
    }

}