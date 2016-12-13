package it.ozimov.springboot.templating.mail.model;

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

public class DefaultEmailSchedulingDataTest {

    @Rule
    public JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void testCompareTo() throws Exception {
        //Arrange
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();
        final DefaultEmailSchedulingData wrapper = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(1)
                .desiredPriority(1)
                .build();
        final DefaultEmailSchedulingData wrapperSmallerPrio = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(2)
                .desiredPriority(2)
                .build();
        final DefaultEmailSchedulingData wrapperBefore = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime.minusDays(1))
                .assignedPriority(1)
                .desiredPriority(1)
                .build();
        final DefaultEmailSchedulingData wrapperAfter = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime.plusDays(1))
                .assignedPriority(1)
                .desiredPriority(1)
                .build();

        //Act+Assert
        assertions.assertThat(wrapper).isLessThan(wrapperSmallerPrio);
        assertions.assertThat(wrapperSmallerPrio).isGreaterThan(wrapper);

        assertions.assertThat(wrapper).isGreaterThan(wrapperBefore);
        assertions.assertThat(wrapperBefore).isLessThan(wrapper);

        assertions.assertThat(wrapper).isLessThan(wrapperAfter);
        assertions.assertThat(wrapperAfter).isGreaterThan(wrapper);

        assertions.assertThat(wrapperSmallerPrio).isLessThan(wrapperAfter);
        assertions.assertThat(wrapperAfter).isGreaterThan(wrapperSmallerPrio);

        assertions.assertThat(wrapperSmallerPrio).isGreaterThan(wrapperBefore);
        assertions.assertThat(wrapperBefore).isLessThan(wrapperSmallerPrio);
    }

}