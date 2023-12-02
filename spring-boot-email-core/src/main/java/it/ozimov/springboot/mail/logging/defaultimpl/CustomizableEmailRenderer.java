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


import it.ozimov.springboot.mail.logging.EmailRenderer;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.EmailAttachment;
import lombok.NonNull;

import jakarta.mail.internet.InternetAddress;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class CustomizableEmailRenderer implements EmailRenderer {

    private static final Collector<CharSequence, ?, String> LIST_JOINER = Collectors.joining(", ", "[", "]");

    private Function<InternetAddress, String> fromFormat;
    private Function<InternetAddress, String> replyToFormat;
    private Function<InternetAddress, String> toFormat;
    private Function<InternetAddress, String> ccFormat;
    private Function<InternetAddress, String> bccFormat;
    private UnaryOperator<String> subjectFormat;
    private UnaryOperator<String> bodyFormat;
    private UnaryOperator<String> attachmentsFormat;
    private UnaryOperator<String> encodingFormat;
    private Function<Locale, String> localeFormat;
    private Function<Date, String> sentAtFormat;
    private Function<InternetAddress, String> receiptToFormat;
    private Function<InternetAddress, String> depositionNotificationToFormat;
    private boolean customHeadersIncluded;
    private boolean includeNullAndEmptyCollections;

    public static CustomizableEmailRendererBuilder builder() {
        return new CustomizableEmailRendererBuilder();
    }

    public static class CustomizableEmailRendererBuilder {

        private CustomizableEmailRenderer customizableEmailRenderer;

        private CustomizableEmailRendererBuilder() {
            this.customizableEmailRenderer = new CustomizableEmailRenderer();
        }

        public CustomizableEmailRendererBuilder withFromFormat(@NonNull final Function<InternetAddress, String> fromFormat) {
            this.customizableEmailRenderer.fromFormat = fromFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withReplyToFormat(@NonNull final Function<InternetAddress, String> replyToFormat) {
            this.customizableEmailRenderer.replyToFormat = replyToFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withToFormat(@NonNull final Function<InternetAddress, String> toFormat) {
            this.customizableEmailRenderer.toFormat = toFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withCcFormat(@NonNull final Function<InternetAddress, String> ccFormat) {
            this.customizableEmailRenderer.ccFormat = ccFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withBccFormat(@NonNull final Function<InternetAddress, String> bccFormat) {
            this.customizableEmailRenderer.bccFormat = bccFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withSubjectFormat(@NonNull final UnaryOperator<String> subjectFormat) {
            this.customizableEmailRenderer.subjectFormat = subjectFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withBodyFormat(@NonNull final UnaryOperator<String> bodyFormat) {
            this.customizableEmailRenderer.bodyFormat = bodyFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withAttachmentsFormat(@NonNull final UnaryOperator<String> attachmentsFormat) {
            this.customizableEmailRenderer.attachmentsFormat = attachmentsFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withEncodingFormat(@NonNull final UnaryOperator<String> encodingFormat) {
            this.customizableEmailRenderer.encodingFormat = encodingFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withLocaleFormat(@NonNull final Function<Locale, String> localeFormat) {
            this.customizableEmailRenderer.localeFormat = localeFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withSentAtFormat(@NonNull final Function<Date, String> sentAtFormat) {
            this.customizableEmailRenderer.sentAtFormat = sentAtFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withReceiptToFormat(@NonNull final Function<InternetAddress, String> receiptToFormat) {
            this.customizableEmailRenderer.receiptToFormat = receiptToFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder withDepositionNotificationToFormat(@NonNull final Function<InternetAddress, String> depositionNotificationToFormat) {
            this.customizableEmailRenderer.depositionNotificationToFormat = depositionNotificationToFormat;
            return this;
        }

        public CustomizableEmailRendererBuilder includeCustomHeaders() {
            this.customizableEmailRenderer.customHeadersIncluded = true;
            return this;
        }

        public CustomizableEmailRendererBuilder includeNullAndEmptyCollections() {
            this.customizableEmailRenderer.includeNullAndEmptyCollections = true;
            return this;
        }

        public CustomizableEmailRenderer build() {
            return this.customizableEmailRenderer;
        }
    }

    @Override
    public String render(@NonNull final Email email) {
        final StringBuilder emailStringBuilder = new StringBuilder("Email{");

        boolean isFirst = true;
        if (nonNull(fromFormat) && !skipField(email.getFrom())) {
            isFirst = false;

            emailStringBuilder.append("from=").append(fromFormat.apply(email.getFrom()));
        }
        if (nonNull(replyToFormat) && !skipField(email.getReplyTo())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("replyTo=").append(replyToFormat.apply(email.getReplyTo()));
        }
        if (nonNull(toFormat) && !skipField(email.getTo())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("to=");
            if(isNull(email.getTo())) {
                emailStringBuilder.append(EmailFieldFormat.nullValue());
            }
            else {
                emailStringBuilder.append(joinAddresses(email.getTo(), toFormat));
            }
        }
        if (nonNull(ccFormat) && !skipField(email.getCc())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("cc=");
            if(isNull(email.getCc())) {
                emailStringBuilder.append(EmailFieldFormat.nullValue());
            }
            else{
                emailStringBuilder.append(joinAddresses(email.getCc(), ccFormat));
            }
        }
        if (nonNull(bccFormat) && !skipField(email.getBcc())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("bcc=");
            if(isNull(email.getBcc())) {
                emailStringBuilder.append(EmailFieldFormat.nullValue());
            }
            else{
                emailStringBuilder.append(joinAddresses(email.getBcc(), bccFormat));
            }
        }
        if (nonNull(subjectFormat) && !skipField(email.getSubject())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("subject=").append(subjectFormat.apply(email.getSubject()));
        }
        if (nonNull(bodyFormat) && !skipField(email.getBody())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("body=").append(bodyFormat.apply(email.getBody()));
        }
        if (nonNull(attachmentsFormat) && !skipField(email.getAttachments())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("attachments=").append(
                    email.getAttachments().stream()
                            .map(EmailAttachment::getAttachmentName).map(attachmentsFormat).collect(LIST_JOINER));
        }
        if (nonNull(encodingFormat) && !skipField(email.getEncoding())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("encoding=").append(encodingFormat.apply(email.getEncoding()));
        }
        if (nonNull(localeFormat) && !skipField(email.getLocale())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("locale=").append(localeFormat.apply(email.getLocale()));
        }
        if (nonNull(sentAtFormat) && !skipField(email.getSentAt())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("sentAt=").append(sentAtFormat.apply(email.getSentAt()));
        }
        if (nonNull(receiptToFormat) && !skipField(email.getReceiptTo())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("receiptTo=").append(receiptToFormat.apply(email.getReceiptTo()));
        }
        if (nonNull(depositionNotificationToFormat) && !skipField(email.getDepositionNotificationTo())) {
            if (!isFirst) emailStringBuilder.append(", ");
            isFirst = false;

            emailStringBuilder.append("depositionNotificationTo=").append(depositionNotificationToFormat.apply(email.getDepositionNotificationTo()));
        }
        if (customHeadersIncluded && !skipField(email.getCustomHeaders())) {
            if (!isFirst) emailStringBuilder.append(", ");

            emailStringBuilder.append("customHeaders=");
            if(isNull(email.getCustomHeaders())) {
                emailStringBuilder.append(EmailFieldFormat.nullValue());
            }
            else{
                emailStringBuilder.append(email.getCustomHeaders().entrySet().stream()
                        .map(entry -> entry.getKey() + '=' + entry.getValue())
                        .collect(LIST_JOINER));
            }

        }
        emailStringBuilder.append('}');
        return emailStringBuilder.toString();
    }

    private boolean skipField(Object object) {
        return !includeNullAndEmptyCollections
                && (isNull(object)
                || (object instanceof Collection && ((Collection) object).isEmpty())
                || (object instanceof Map && ((Map) object).isEmpty())
        );
    }


    private String joinAddresses(final Collection<InternetAddress> internetAddressCollection, final Function<InternetAddress, String> internetAddressStringFormatter) {
        return internetAddressCollection.stream()
                .map(internetAddressStringFormatter)
                .collect(LIST_JOINER);
    }

}
