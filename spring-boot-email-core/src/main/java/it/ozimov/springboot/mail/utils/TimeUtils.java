package it.ozimov.springboot.mail.utils;

import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;

import static java.time.ZoneOffset.UTC;

@UtilityClass
public class TimeUtils {

    public static long now() {
        return offsetDateTimeNow().toInstant().toEpochMilli();
    }

    public static OffsetDateTime offsetDateTimeNow() {
        return OffsetDateTime.now(UTC);
    }

}
