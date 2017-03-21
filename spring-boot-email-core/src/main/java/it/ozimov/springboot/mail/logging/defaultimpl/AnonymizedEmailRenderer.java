//package it.ozimov.springboot.mail.logging.defaultimpl;
//
//
//import it.ozimov.springboot.mail.logging.EmailRenderer;
//import it.ozimov.springboot.mail.model.Email;
//import it.ozimov.springboot.mail.model.EmailAttachment;
//import lombok.NonNull;
//import lombok.Singular;
//import org.springframework.stereotype.Component;
//
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//
//@Component
//public class AnonymizedEmailRenderer implements EmailRenderer {
//
//    private final List<String> ignoredFields;
//    private final List<String> anonymizedFields;
//
//    public AnonymizedEmailRenderer(List<String> ignoredFields, List<String> anonymizedFields) {
//        this.ignoredFields = ignoredFields;
//        this.anonymizedFields = anonymizedFields;
//    }
//
//    @Override
//    public String render(@NonNull final Email email) {
//        final StringBuilder emailStringBuilder = new StringBuilder("Email{");
//        if(processFrom())
//        emailStringBuilder.append(", from=").append(handleFrom(email.getFrom()));
//        if(processReplyTo())
//        emailStringBuilder.append(", replyTo=").append(handleReplyTo(email.getReplyTo()));
//        if(processTo())
//        emailStringBuilder.append(", to=").append(handleTo(email.getTo()));
//        if(processCC())
//        emailStringBuilder.append(", cc=").append(handleCc(email.getCc()));
//        if(processBcc())
//        emailStringBuilder.append(", bcc=").append(handleBcc(email.getBcc()));
//        if(processSubject())
//        emailStringBuilder.append(", subject='").append(handleSubject(email.getSubject())).append('\'');
//        if(processBody())
//        emailStringBuilder.append(", body='").append(handleBody(email.getBody())).append('\'');
//        if(processAttachments())
//        emailStringBuilder.append(", attachments=").append(handleAttachments(email.getAttachments()));
//        if(processEncoding())
//        emailStringBuilder.append(", encoding='").append(handleEncoding(email.getEncoding())).append('\'');
//        if(processLocale())
//        emailStringBuilder.append(", locale=").append(handleLocale(email.getLocale()));
//        if(processSentAt())
//        emailStringBuilder.append(", sentAt=").append(handleSentAt(email.getSentAt()));
//        if(processReceiptTo())
//        emailStringBuilder.append(", receiptTo=").append(handleReceiptTo(email.getReceiptTo()));
//        if(processDepositionNotificationTo())
//        emailStringBuilder.append(", depositionNotificationTo=").append(handleDepositionNotificationTo(email.getDepositionNotificationTo()));
//        if(processCustomHeaders())
//        emailStringBuilder.append(", customHeaders=").append(handleCustomHeaders(email.getCustomHeaders()));
//        emailStringBuilder.append('}');
//        return emailStringBuilder.toString();
//    }
//
//    private static void appendText()
//
//    private char[] handleFrom(InternetAddress from) {
//    }
//
//    private char[] handleReplyTo(InternetAddress replyTo) {
//    }
//
//    private char[] handleTo(Collection<InternetAddress> to) {
//    }
//
//    private char[] handleCc(Collection<InternetAddress> cc) {
//    }
//
//    private char[] handleBcc(Collection<InternetAddress> bcc) {
//    }
//
//    private char[] handleSubject(String subject) {
//    }
//
//    private char[] handleBody(String body) {
//    }
//
//    private char[] handleAttachments(Collection<EmailAttachment> attachments) {
//    }
//
//    private char[] handleEncoding(String encoding) {
//    }
//
//    private char[] handleLocale(Locale locale) {
//    }
//
//    private char[] handleSentAt(Date sentAt) {
//    }
//
//    private char[] handleReceiptTo(InternetAddress receiptTo) {
//    }
//
//    private char[] handleDepositionNotificationTo(InternetAddress depositionNotificationTo) {
//    }
//
//    private char[] handleCustomHeaders(Map<String, String> customHeaders) {
//    }
//
//
//    private boolean processFrom() {
//    }
//
//    private boolean processReplyTo() {
//    }
//
//    private boolean processTo() {
//    }
//
//    private boolean processCC() {
//    }
//
//    private boolean processBcc() {
//    }
//
//    private boolean processSubject() {
//    }
//
//    private boolean processBody() {
//    }
//
//    private boolean processAttachments() {
//    }
//
//    private boolean processEncoding() {
//    }
//
//    private boolean processLocale() {
//    }
//
//    private boolean processSentAt() {
//    }
//
//    private boolean processReceiptTo() {
//    }
//
//    private boolean processDepositionNotificationTo() {
//    }
//
//    private boolean processCustomHeaders() {
//    }
//
//    @Override
//    public String render(@NonNull final MimeMessage mimeMessage) {
//        return null;
//    }
//}
