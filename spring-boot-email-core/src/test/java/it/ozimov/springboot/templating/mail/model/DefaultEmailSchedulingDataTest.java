package it.ozimov.springboot.templating.mail.model;

import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.templating.mail.utils.TimeUtils;
import org.junit.Test;

import java.time.OffsetDateTime;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.getSimpleMail;
import static org.hamcrest.CoreMatchers.is;

public class DefaultEmailSchedulingDataTest {

    @Test
    public void testCompareTo() throws Exception {
        //Arrange
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();
        final DefaultEmailSchedulingData wrapper = DefaultEmailSchedulingData.builder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .priority(1)
                .build();
        final DefaultEmailSchedulingData wrapperSmallerPrio = DefaultEmailSchedulingData.builder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .priority(2)
                .build();
        final DefaultEmailSchedulingData wrapperBefore = DefaultEmailSchedulingData.builder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime.minusDays(1))
                .priority(1)
                .build();
        final DefaultEmailSchedulingData wrapperAfter = DefaultEmailSchedulingData.builder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime.plusDays(1))
                .priority(1)
                .build();

        //Act+Assert
        given(wrapper.compareTo(wrapperSmallerPrio)).assertThat(is(-1));
        given(wrapperSmallerPrio.compareTo(wrapper)).assertThat(is(1));

        given(wrapper.compareTo(wrapperBefore)).assertThat(is(1));
        given(wrapperBefore.compareTo(wrapper)).assertThat(is(-1));

        given(wrapper.compareTo(wrapperAfter)).assertThat(is(-1));
        given(wrapperAfter.compareTo(wrapper)).assertThat(is(1));

        given(wrapperSmallerPrio.compareTo(wrapperAfter)).assertThat(is(-1));
        given(wrapperAfter.compareTo(wrapperSmallerPrio)).assertThat(is(1));

        given(wrapperSmallerPrio.compareTo(wrapperBefore)).assertThat(is(1));
        given(wrapperBefore.compareTo(wrapperSmallerPrio)).assertThat(is(-1));
    }

}