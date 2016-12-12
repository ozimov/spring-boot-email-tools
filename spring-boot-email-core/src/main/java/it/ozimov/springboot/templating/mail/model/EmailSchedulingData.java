package it.ozimov.springboot.templating.mail.model;


import it.ozimov.springboot.templating.mail.utils.TimeUtils;

import java.io.Serializable;
import java.time.OffsetDateTime;

public interface EmailSchedulingData extends Comparable<EmailSchedulingData>, Serializable {

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
        final OffsetDateTime thisScheduledDateTime = getScheduledDateTime();
        if (thisScheduledDateTime.isBefore(o.getScheduledDateTime())) {
            return -1;
        } else if (thisScheduledDateTime.isAfter(o.getScheduledDateTime())) {
            return 1;
        } else {
            return Integer.compare(this.getDesiredPriority(), o.getDesiredPriority());
        }
    }

}