package open.springboot.mail.model;

import open.springboot.mail.utils.TimeUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static open.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class EmailSchedulingWrapperTest {

    @Test
    public void testCompareTo() throws Exception {
        //Arrange
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();
        final EmailSchedulingWrapper wrapper = EmailSchedulingWrapper.builder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .priority(1)
                .build();
        final EmailSchedulingWrapper wrapperSmallerPrio = EmailSchedulingWrapper.builder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .priority(2)
                .build();
        final EmailSchedulingWrapper wrapperBefore = EmailSchedulingWrapper.builder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime.minusDays(1))
                .priority(1)
                .build();
        final EmailSchedulingWrapper wrapperAfter = EmailSchedulingWrapper.builder()
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