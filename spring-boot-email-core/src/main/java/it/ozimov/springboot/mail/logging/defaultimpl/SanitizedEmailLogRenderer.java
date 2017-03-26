package it.ozimov.springboot.mail.logging.defaultimpl;

import it.ozimov.springboot.mail.logging.EmailLogRenderer;
import it.ozimov.springboot.mail.logging.EmailRenderer;
import it.ozimov.springboot.mail.model.Email;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Scope("prototype")
@Slf4j
public class SanitizedEmailLogRenderer implements EmailLogRenderer {

    private Logger logger;

    private final EmailRenderer emailRenderer;

    @Autowired
    public SanitizedEmailLogRenderer(final EmailRenderer emailRenderer) {
        logger = log;
        this.emailRenderer = emailRenderer;
    }

    @Override
    public void registerLogger(@NonNull final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void trace(@NonNull final String message, @NonNull final Email email, Objects... messageParams) {
        if (logger.isTraceEnabled()) {
            logger.trace(message, renderEmail(email), messageParams);
        }
    }

    @Override
    public void trace(@NonNull final Email email) {
        if (logger.isTraceEnabled()) {
            logger.trace(renderEmail(email));
        }
    }

    @Override
    public void debug(@NonNull final String message, @NonNull final Email email, Objects... messageParams) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, renderEmail(email), messageParams);

        }
    }

    @Override
    public void debug(@NonNull final Email email) {
        if (logger.isDebugEnabled()) {
            logger.debug(renderEmail(email));
        }
    }

    @Override
    public void info(@NonNull final String message, @NonNull final Email email, Objects... messageParams) {
        if (logger.isInfoEnabled()) {
            logger.info(message, renderEmail(email), messageParams);
        }
    }

    @Override
    public void info(@NonNull final Email email) {
        if (logger.isInfoEnabled()) {
            logger.info(renderEmail(email));
        }
    }

    @Override
    public void warn(@NonNull final String message, @NonNull final Email email, Objects... messageParams) {
        if (logger.isWarnEnabled()) {
            logger.warn(message, renderEmail(email), messageParams);
        }
    }

    @Override
    public void warn(@NonNull final Email email) {
        if (logger.isWarnEnabled()) {
            logger.warn(renderEmail(email));
        }
    }

    @Override
    public void error(@NonNull final String message, @NonNull final Email email, Objects... messageParams) {
        if (logger.isErrorEnabled()) {
            logger.error(message, renderEmail(email), messageParams);
        }
    }

    @Override
    public void error(@NonNull final Email email) {
        if (logger.isErrorEnabled()) {
            logger.error(renderEmail(email));
        }
    }

    private String renderEmail(final Email email) {
        return sanitizeString(emailRenderer.render(email));
    }

    /**
     * Sanitize text to prevent injection of EOL characters into log messages.
     * <p>
     * Actually, replaces all whitespaces including CR and LF with a space and all other ASCII control
     * characters with "?" to avoid malicious code in the logs.
     */
    private static String sanitizeString(String text) {
        return Encode.forJava(text);
    }

}
