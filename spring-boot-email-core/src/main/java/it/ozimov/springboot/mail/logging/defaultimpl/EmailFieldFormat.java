package it.ozimov.springboot.mail.logging.defaultimpl;

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;

import javax.mail.internet.InternetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.util.Objects.isNull;

@UtilityClass
public class EmailFieldFormat {

    private static final String STARS = "***";

    private static final String NULL = "NULL";

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat DATE_FORMAT_WITH_ZONE_ID = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public static String plainText(final Locale locale) {
        return plainText(isNull(locale)? null : locale.toString());
    }

    public static String plainText(final InternetAddress internetAddress) {
        return plainText(isNull(internetAddress)? null : internetAddress.getAddress());
    }

    public static String plainText(final String text) {
        if (isNull(text)) return NULL;
        return text;
    }

    public static String firstDozenThenStars(final InternetAddress internetAddress) {
        return firstDozenThenStars(isNull(internetAddress) ? null : internetAddress.getAddress());
    }

    public static String firstDozenThenStars(final String text) {
        if (isNull(text)) return NULL;
        return text.length() > 12 ?
                appendStars(text, 0, 12) : text;
    }

    public static String textFromAt(final InternetAddress internetAddress) {
        return textFromAt(isNull(internetAddress) ? null : internetAddress.getAddress());
    }

    public static String textFromAt(final String text) {
        if (isNull(text)) return NULL;
        final int indexOfAt = text.indexOf('@');
        Preconditions.checkArgument(indexOfAt>0,
                "Given text should contain '@', while %s given.", text);
        return preprendStars(text, indexOfAt, text.length());
    }

    public static String textUpToAt(final InternetAddress internetAddress) {
        return textUpToAt(isNull(internetAddress) ? null : internetAddress.getAddress());
    }

    public static String textUpToAt(final String text) {
        if (isNull(text)) return NULL;
        final int indexOfAt = text.indexOf('@');
        Preconditions.checkArgument(indexOfAt>0,
                "Given text should contain '@', while %s given.", text);
        return appendStars(text, 0, indexOfAt + 1);
    }

    public static String dateFormat(final Date date) {
        if (isNull(date)) return NULL;
        return SIMPLE_DATE_FORMAT.format(date);
    }

    public static String dateFormatWithZoneId(final Date date) {
        if (isNull(date)) return NULL;
        return DATE_FORMAT_WITH_ZONE_ID.format(date);
    }

    private static String appendStars(String text, int from, int to) {
        return text.substring(from, to) + STARS;
    }

    private static String preprendStars(String text, int from, int to) {
        return STARS + text.substring(from, to);
    }

}
