/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.ozimov.springboot.mail.logging.defaultimpl;

import com.google.common.base.Preconditions;
import it.ozimov.springboot.mail.logging.LoggingStrategy;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.mail.internet.InternetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;

@UtilityClass
@Slf4j
public class EmailFieldFormat {

    private static final String STARS = "***";

    private static final String NULL = "NULL";

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = createSimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat DATE_FORMAT_WITH_ZONE_ID = createSimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public static UnaryOperator<String> textFormatterFrom(@NonNull final LoggingStrategy loggingStrategy) {
        switch (loggingStrategy) {
            case PLAIN_TEXT:
                return EmailFieldFormat::plainText;
            case HIDDEN:
                break;
            case FIRST_DOZEN_THEN_STARS:
                return EmailFieldFormat::firstDozenThenStars;
            case FULL_TEXT_FROM_COMMERCIAL_AT:
            case FULL_TEXT_UP_TO_COMMERCIAL_AT:
            case STANDARD_DATE_FORMAT:
            case STANDARD_DATE_FORMAT_WITH_ZONE_ID:
            default:
                logUnsupportedLoggingStrategy(loggingStrategy, "String");
        }
        return null;
    }

    public static Function<InternetAddress, String> emailFormatterFrom(@NonNull final LoggingStrategy loggingStrategy) {
        switch (loggingStrategy) {
            case PLAIN_TEXT:
                return EmailFieldFormat::plainText;
            case HIDDEN:
                break;
            case FIRST_DOZEN_THEN_STARS:
                return EmailFieldFormat::firstDozenThenStars;
            case FULL_TEXT_FROM_COMMERCIAL_AT:
                return EmailFieldFormat::textFromAt;
            case FULL_TEXT_UP_TO_COMMERCIAL_AT:
                return EmailFieldFormat::textUpToAt;
            case STANDARD_DATE_FORMAT:
            case STANDARD_DATE_FORMAT_WITH_ZONE_ID:
            default:
                logUnsupportedLoggingStrategy(loggingStrategy, "InternetAddress");
        }
        return null;
    }

    public static Function<Locale, String> localeFormatterFrom(@NonNull final LoggingStrategy loggingStrategy) {
        switch (loggingStrategy) {
            case PLAIN_TEXT:
                return EmailFieldFormat::plainText;
            case HIDDEN:
                break;
            case FIRST_DOZEN_THEN_STARS:
            case FULL_TEXT_FROM_COMMERCIAL_AT:
            case FULL_TEXT_UP_TO_COMMERCIAL_AT:
            case STANDARD_DATE_FORMAT:
            case STANDARD_DATE_FORMAT_WITH_ZONE_ID:
            default:
                logUnsupportedLoggingStrategy(loggingStrategy, "Locale");
        }
        return null;
    }

    public static Function<Date, String> dateFormatterFrom(@NonNull final LoggingStrategy loggingStrategy) {
        switch (loggingStrategy) {
            case PLAIN_TEXT:
                break;
            case HIDDEN:
                break;
            case STANDARD_DATE_FORMAT:
                return EmailFieldFormat::dateFormat;
            case STANDARD_DATE_FORMAT_WITH_ZONE_ID:
                return EmailFieldFormat::dateFormatWithZoneId;
            case FIRST_DOZEN_THEN_STARS:
            case FULL_TEXT_FROM_COMMERCIAL_AT:
            case FULL_TEXT_UP_TO_COMMERCIAL_AT:
            default:
                logUnsupportedLoggingStrategy(loggingStrategy, "Date");
        }
        return null;
    }

    public static String plainText(final Locale locale) {
        return plainText(isNull(locale) ? null : locale.toString());
    }


    public static String plainText(final InternetAddress internetAddress) {
        return plainText(isNull(internetAddress) ? null : internetAddress.getAddress());
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
        Preconditions.checkArgument(indexOfAt > 0,
                "Given text should contain '@', while %s given.", text);
        return prependStars(text, indexOfAt, text.length());
    }

    public static String textUpToAt(final InternetAddress internetAddress) {
        return textUpToAt(isNull(internetAddress) ? null : internetAddress.getAddress());
    }

    public static String textUpToAt(final String text) {
        if (isNull(text)) return NULL;
        final int indexOfAt = text.indexOf('@');
        Preconditions.checkArgument(indexOfAt > 0,
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

    public static String nullValue() {
        return NULL;
    }

    private static String appendStars(String text, int from, int to) {
        return text.substring(from, to) + STARS;
    }

    private static String prependStars(String text, int from, int to) {
        return STARS + text.substring(from, to);
    }

    private static SimpleDateFormat createSimpleDateFormat(String pattern) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat;
    }

    private static void logUnsupportedLoggingStrategy(LoggingStrategy loggingStrategy, String inputType) {
        log.warn("LoggingStrategy {} is not supported for the input type {}.", loggingStrategy, inputType);
    }

}
