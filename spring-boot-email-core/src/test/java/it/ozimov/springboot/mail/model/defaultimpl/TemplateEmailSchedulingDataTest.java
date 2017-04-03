package it.ozimov.springboot.mail.model.defaultimpl;

import com.google.common.collect.ImmutableMap;
import it.ozimov.springboot.mail.model.InlinePicture;
import it.ozimov.springboot.mail.utils.TimeUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.time.OffsetDateTime;

import static it.ozimov.springboot.mail.utils.EmailToMimeMessageTest.getSimpleMail;

public class TemplateEmailSchedulingDataTest {

    @Rule
    public JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldGetScheduledDateTimeUseNowAsScheduledTime() throws Exception {
        //Arrange
        OffsetDateTime timeBefore = TimeUtils.offsetDateTimeNow();
        final TemplateEmailSchedulingData emailSchedulingData = TemplateEmailSchedulingData.templateEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .template("template.html")
                .modelObject(ImmutableMap.of())
                .inlinePictures(new InlinePicture[]{})
                .assignedPriority(1)
                .desiredPriority(1)
                .build();
        OffsetDateTime timeAfter = TimeUtils.offsetDateTimeNow();

        //Act
        OffsetDateTime givenDefaultScheduledTime = emailSchedulingData.getScheduledDateTime();

        //Assert
        assertions.assertThat(givenDefaultScheduledTime)
                .isAfterOrEqualTo(timeBefore)
                .isBeforeOrEqualTo(timeAfter);
    }

}