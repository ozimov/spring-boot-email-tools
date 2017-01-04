package it.ozimov.springboot.templating.mail.utils;

import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Created by ssanchezc on 23/12/2016.
 */
public class MimeMessageHelperExt extends MimeMessageHelper {

    private static final String HEADER_DEPOSITION_NOTIFICATION_TO = "Disposition-Notification-To";

    private static final String HEADER_RETURN_RECIPT = "Return-Receipt-To";

    public MimeMessageHelperExt(MimeMessage mimeMessage) {
        super(mimeMessage);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, String encoding) {
        super(mimeMessage, encoding);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, boolean multipart) throws MessagingException {
        super(mimeMessage, multipart);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, boolean multipart, String encoding) throws MessagingException {
        super(mimeMessage, multipart, encoding);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, int multipartMode) throws MessagingException {
        super(mimeMessage, multipartMode);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, int multipartMode, String encoding) throws MessagingException {
        super(mimeMessage, multipartMode, encoding);
    }



    public void setHeaderReturnRecipt(String emailToNotification) throws MessagingException {
        getMimeMessage().setHeader(HEADER_RETURN_RECIPT, emailToNotification);
    }
    public void setHeaderDepositionNotificationTo(String emailToNotification) throws MessagingException {
        getMimeMessage().setHeader(HEADER_DEPOSITION_NOTIFICATION_TO, emailToNotification);
    }
}
