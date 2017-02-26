package it.ozimov.springboot.mail.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.mail.internet.MimeMessage;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MimeMessageHelperExtTest {

    private static final String HEADER_DEPOSITION_NOTIFICATION_TO = "Disposition-Notification-To";

    private static final String HEADER_RETURN_RECEIPT = "Return-Receipt-To";

    @Mock
    public MimeMessage mimeMessage;

    @Test
    public void setHeaderReturnReceipt() throws Exception {
        //Arrange
        MimeMessageHelperExt mimeMessageHelperExt = new MimeMessageHelperExt(mimeMessage);
        String expectedReturnReceipt = "marco-tullio@roma.aeterna";

        //Act
        mimeMessageHelperExt.setHeaderReturnReceipt(expectedReturnReceipt);

        //Assert
        verify(mimeMessage).setHeader(HEADER_RETURN_RECEIPT, expectedReturnReceipt);
    }

    @Test
    public void setHeaderDepositionNotificationTo() throws Exception {
        //Arrange
        MimeMessageHelperExt mimeMessageHelperExt = new MimeMessageHelperExt(mimeMessage);
        String expectedDepositionNotificationTo = "marco-tullio@roma.aeterna";

        //Act
        mimeMessageHelperExt.setHeaderDepositionNotificationTo(expectedDepositionNotificationTo);

        //Assert
        verify(mimeMessage).setHeader(HEADER_DEPOSITION_NOTIFICATION_TO, expectedDepositionNotificationTo);
    }

}