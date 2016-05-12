package it.ozimov.springboot.mail.model;

import it.ozimov.springboot.mail.utils.EmailToMimeMessageTest;
import it.ozimov.springboot.mail.utils.TimeUtils;
import org.junit.Test;

import java.time.OffsetDateTime;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static it.ozimov.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;
import static org.hamcrest.CoreMatchers.is;

public class EmailSchedulingWrapperTest {

    @Test
    public void testCompareTo() throws Exception {
        //Arrange
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();
        final EmailSchedulingWrapper wrapper = EmailSchedulingWrapper.builder()
                .email(EmailToMimeMessageTest.getSimpleMail())
                .scheduledDateTime(dateTime)
                .priority(1)
                .build();
        final EmailSchedulingWrapper wrapperSmallerPrio = EmailSchedulingWrapper.builder()
                .email(EmailToMimeMessageTest.getSimpleMail())
                .scheduledDateTime(dateTime)
                .priority(2)
                .build();
        final EmailSchedulingWrapper wrapperBefore = EmailSchedulingWrapper.builder()
                .email(EmailToMimeMessageTest.getSimpleMail())
                .scheduledDateTime(dateTime.minusDays(1))
                .priority(1)
                .build();
        final EmailSchedulingWrapper wrapperAfter = EmailSchedulingWrapper.builder()
                .email(EmailToMimeMessageTest.getSimpleMail())
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