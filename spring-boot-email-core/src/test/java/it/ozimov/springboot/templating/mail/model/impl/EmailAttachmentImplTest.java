package it.ozimov.springboot.templating.mail.model.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import testutils.TestUtils;

import java.io.File;
import java.io.IOException;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.IMAGE_JPEG;

public class EmailAttachmentImplTest {

    private static final String FILE_PATH = "images" + File.separator + "100_percent_free.jpg";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEmailAttachmentImplMustHaveAttachmentName() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        EmailAttachmentImpl.builder()
                .attachmentData(TestUtils.loadFileIntoByte(FILE_PATH))
                .build();

        //Assert
        fail();
    }

    @Test
    public void testEmailAttachmentImplMustHaveAttachmentData() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        EmailAttachmentImpl.builder()
                .attachmentName("Attachment.jpg")
                .build();

        //Assert
        fail();
    }

    @Test
    public void testEmailAttachmentImplValidWithRequiredFields() throws Exception {
        //Arrange

        //Act
        final EmailAttachmentImpl emailAttachment = getEmailAttachmentImpl();

        //Assert
        assertThat(emailAttachment, not(is(nullValue())));
    }


    @Test
    public void testGetInputStream() throws Exception {
        //Arrange
        final EmailAttachmentImpl emailAttachment = getEmailAttachmentImpl();

        //Act
        final ByteArrayResource byteArrayResource = emailAttachment.getInputStream();

        //Assert
        assertThat(byteArrayResource, not(is(nullValue())));
        assertThat(byteArrayResource.exists(), is(true));
    }

    private EmailAttachmentImpl getEmailAttachmentImpl() throws IOException {
        return EmailAttachmentImpl.builder()
                .attachmentName("Attachment.jpg")
                .attachmentData(TestUtils.loadFileIntoByte(FILE_PATH))
                .build();
    }


}