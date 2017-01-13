package it.ozimov.springboot.templating.mail.service.defaultimpl;

import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.templating.mail.utils.TimeUtils;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;

import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.getSimpleMail;

public class EmailSchedulingDataUtils {

    public static DefaultEmailSchedulingData createDefaultEmailSchedulingDataWithPriority(final int assignedPriority) throws UnsupportedEncodingException {
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();

        final DefaultEmailSchedulingData defaultEmailSchedulingData = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(assignedPriority)
                .desiredPriority(assignedPriority)
                .build();
        return defaultEmailSchedulingData;
    }

}