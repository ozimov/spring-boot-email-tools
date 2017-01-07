package it.ozimov.springboot.templating.mail.model;


import it.ozimov.springboot.templating.mail.utils.TimeUtils;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Comparator;

public interface EmailSchedulingData extends Comparable<EmailSchedulingData>, Serializable {

    Comparator<EmailSchedulingData> DEFAULT_COMPARATOR = (o1, o2) -> {
        final OffsetDateTime o1ScheduledDateTime = o1.getScheduledDateTime();
        final OffsetDateTime o2ScheduledDateTime = o2.getScheduledDateTime();
        if (o1ScheduledDateTime.isBefore(o2ScheduledDateTime)) {
            return -1;
        } else if (o1ScheduledDateTime.isAfter(o2ScheduledDateTime)) {
            return 1;
        } else {
            return Integer.compare(o1.getDesiredPriority(), o2.getDesiredPriority());
        }
    };

    String getId();

    Email getEmail();

    default int getDesiredPriority() {
        return 0;
    }

    default int getAssignedPriority() {
        return 0;
    }

    default OffsetDateTime getScheduledDateTime() {
        return TimeUtils.offsetDateTimeNow();
    }

    default int compareTo(EmailSchedulingData o) {
        return DEFAULT_COMPARATOR.compare(this, o);
    }

}