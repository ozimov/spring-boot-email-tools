package it.ozimov.springboot.templating.mail.service.defaultimpl;

import com.google.common.collect.ImmutableMap;
import it.ozimov.springboot.templating.mail.model.ImageType;
import it.ozimov.springboot.templating.mail.model.InlinePicture;
import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultInlinePicture;
import it.ozimov.springboot.templating.mail.model.defaultimpl.TemplateEmailSchedulingData;
import it.ozimov.springboot.templating.mail.utils.TimeUtils;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;

import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.getSimpleMail;

public class EmailSchedulingDataUtils {

    public static DefaultEmailSchedulingData createDefaultEmailSchedulingDataWithPriority(final int assignedPriority) throws UnsupportedEncodingException {
        return createDefaultEmailSchedulingDataWithPriority(assignedPriority, 0);
    }

    public static DefaultEmailSchedulingData createDefaultEmailSchedulingDataWithPriority(final int assignedPriority, final long nanosFromNow) throws UnsupportedEncodingException {
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow().plusNanos(nanosFromNow);

        final DefaultEmailSchedulingData defaultEmailSchedulingData = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(assignedPriority)
                .desiredPriority(assignedPriority)
                .build();
        return defaultEmailSchedulingData;
    }

    public static TemplateEmailSchedulingData createTemplateEmailSchedulingDataWithPriority(final int assignedPriority) throws UnsupportedEncodingException {
        return createTemplateEmailSchedulingDataWithPriority(assignedPriority, 0);
    }

    public static TemplateEmailSchedulingData createTemplateEmailSchedulingDataWithPriority(final int assignedPriority, final long nanosFromNow) throws UnsupportedEncodingException {
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow().plusNanos(nanosFromNow);

        final TemplateEmailSchedulingData templateEmailSchedulingData = TemplateEmailSchedulingData.templateEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .template("template.html")
                .modelObject(ImmutableMap.of("key", "var"))
                .inlinePictures(new InlinePicture[]{})
                .scheduledDateTime(dateTime)
                .assignedPriority(assignedPriority)
                .desiredPriority(assignedPriority)
                .build();
        return templateEmailSchedulingData;
    }
    
}