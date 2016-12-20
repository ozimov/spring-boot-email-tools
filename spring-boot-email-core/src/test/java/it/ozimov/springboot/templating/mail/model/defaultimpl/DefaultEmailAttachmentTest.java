package it.ozimov.springboot.templating.mail.model.defaultimpl;

import it.ozimov.springboot.templating.mail.UnitTest;
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

public class DefaultEmailAttachmentTest implements UnitTest {

    private static final String FILE_PATH = "images" + File.separator + "100_percent_free.jpg";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEmailAttachmentImplMustHaveAttachmentName() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        DefaultEmailAttachment.builder()
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
        DefaultEmailAttachment.builder()
                .attachmentName("Attachment.jpg")
                .build();

        //Assert
        fail();
    }

    @Test
    public void testEmailAttachmentImplValidWithRequiredFields() throws Exception {
        //Arrange

        //Act
        final DefaultEmailAttachment emailAttachment = getEmailAttachmentImpl();

        //Assert
        assertThat(emailAttachment, not(is(nullValue())));
    }


    @Test
    public void testGetInputStream() throws Exception {
        //Arrange
        final DefaultEmailAttachment emailAttachment = getEmailAttachmentImpl();

        //Act
        final ByteArrayResource byteArrayResource = emailAttachment.getInputStream();

        //Assert
        assertThat(byteArrayResource, not(is(nullValue())));
        assertThat(byteArrayResource.exists(), is(true));
    }

    @Test
    public void testGetContentType() throws Exception {
        //Arrange
        final DefaultEmailAttachment emailAttachment = getEmailAttachmentImpl();

        //Act
        final MediaType mediaType = emailAttachment.getContentType();

        //Assert
        assertThat(mediaType, not(is(nullValue())));
        assertThat(mediaType, is(IMAGE_JPEG));
    }

    private DefaultEmailAttachment getEmailAttachmentImpl() throws IOException {
        return DefaultEmailAttachment.builder()
                .attachmentName("Attachment.jpg")
                .attachmentData(TestUtils.loadFileIntoByte(FILE_PATH))
                .build();
    }


}