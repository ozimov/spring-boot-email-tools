package it.ozimov.springboot.mail.configuration;

import it.ozimov.springboot.mail.logging.EmailRenderer;
import it.ozimov.springboot.mail.logging.LoggingStrategy;
import it.ozimov.springboot.mail.logging.defaultimpl.CustomizableEmailRenderer;
import it.ozimov.springboot.mail.logging.defaultimpl.CustomizableEmailRenderer.CustomizableEmailRendererBuilder;
import it.ozimov.springboot.mail.logging.defaultimpl.EmailFieldFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.mail.internet.InternetAddress;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static it.ozimov.springboot.mail.service.defaultimpl.ConditionalExpression.EMAIL_LOGGING_RENDERER_IS_ENABLED;
import static java.util.Objects.nonNull;

@Configuration
@ConditionalOnExpression(EMAIL_LOGGING_RENDERER_IS_ENABLED)
public class EmailRenderedConfiguration {

    private final LoggingProperties loggingProperties;

    @Autowired
    public EmailRenderedConfiguration(final LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @Bean
    public EmailRenderer createEmailRenderer() {
        final CustomizableEmailRendererBuilder customizableEmailRenderer = CustomizableEmailRenderer.builder();
        final LoggingProperties.Strategy loggingPropertiesStrategy = loggingProperties.getStrategy();

        final Function<InternetAddress, String> fromFormat = EmailFieldFormat.emailFormatterFrom(loggingPropertiesStrategy.getFrom());
        if (nonNull(fromFormat)) customizableEmailRenderer.withFromFormat(fromFormat);

        final Function<InternetAddress, String> replyToFormat = EmailFieldFormat.emailFormatterFrom(loggingPropertiesStrategy.getReplyTo());
        if (nonNull(replyToFormat)) customizableEmailRenderer.withFromFormat(replyToFormat);

        final Function<InternetAddress, String> toFormat = EmailFieldFormat.emailFormatterFrom(loggingPropertiesStrategy.getTo());
        if (nonNull(toFormat)) customizableEmailRenderer.withToFormat(toFormat);

        final Function<InternetAddress, String> ccFormat = EmailFieldFormat.emailFormatterFrom(loggingPropertiesStrategy.getCc());
        if (nonNull(ccFormat)) customizableEmailRenderer.withCcFormat(ccFormat);

        final Function<InternetAddress, String> bccFormat = EmailFieldFormat.emailFormatterFrom(loggingPropertiesStrategy.getBcc());
        if (nonNull(bccFormat)) customizableEmailRenderer.withBccFormat(bccFormat);

        final UnaryOperator<String> subjectFormat = EmailFieldFormat.textFormatterFrom(loggingPropertiesStrategy.getSubject());
        if (nonNull(subjectFormat)) customizableEmailRenderer.withSubjectFormat(subjectFormat);
        
        final UnaryOperator<String> bodyFormat = EmailFieldFormat.textFormatterFrom(loggingPropertiesStrategy.getBody());
        if (nonNull(bodyFormat)) customizableEmailRenderer.withBodyFormat(bodyFormat);
        
        final UnaryOperator<String> attachmentsFormat = EmailFieldFormat.textFormatterFrom(loggingPropertiesStrategy.getAttachments());
        if (nonNull(attachmentsFormat)) customizableEmailRenderer.withAttachmentsFormat(attachmentsFormat);
        
        final UnaryOperator<String> encodingFormat = EmailFieldFormat.textFormatterFrom(loggingPropertiesStrategy.getEncoding());
        if (nonNull(encodingFormat)) customizableEmailRenderer.withEncodingFormat(encodingFormat);
        
        final Function<Locale, String> localeFormat = EmailFieldFormat.localeFormatterFrom(loggingPropertiesStrategy.getLocale());
        if (nonNull(localeFormat)) customizableEmailRenderer.withLocaleFormat(localeFormat);
        
        final Function<Date, String> sentAtFormat = EmailFieldFormat.dateFormatterFrom(loggingPropertiesStrategy.getSentAt());
        if (nonNull(sentAtFormat)) customizableEmailRenderer.withSentAtFormat(sentAtFormat);

        final Function<InternetAddress, String> receiptToFormat = EmailFieldFormat.emailFormatterFrom(loggingPropertiesStrategy.getReceiptTo());
        if (nonNull(receiptToFormat)) customizableEmailRenderer.withReceiptToFormat(receiptToFormat);
        
        final Function<InternetAddress, String> depositionNotificationToFormat = EmailFieldFormat.emailFormatterFrom(loggingPropertiesStrategy.getDepositionNotificationTo());
        if (nonNull(depositionNotificationToFormat)) customizableEmailRenderer.withDepositionNotificationToFormat(depositionNotificationToFormat);

        if (!loggingPropertiesStrategy.areCustomHeadersIgnored()) customizableEmailRenderer.includeCustomHeaders();

        if (!loggingPropertiesStrategy.areNullAndEmptyCollectionsIgnored()) customizableEmailRenderer.includeNullAndEmptyCollections();

        return customizableEmailRenderer.build();
    }


}
