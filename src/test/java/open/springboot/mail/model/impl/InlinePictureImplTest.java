package open.springboot.mail.model.email.impl;

import open.springboot.mail.model.impl.InlinePictureImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static open.springboot.mail.model.ImageType.JPEG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class InlinePictureImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private File mockFile;

    @Test
    public void testInlinePictureImplMustHaveImageType() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        InlinePictureImpl.builder()
                .file(mockFile)
                .templateName("template.ftl")
                .build();

        //Assert
        fail("Expected a NullPointerException for missing mandatory field");
    }

    @Test
    public void testInlinePictureImplMustHaveFile() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        InlinePictureImpl.builder()
                .imageType(JPEG)
                .templateName("template.ftl")
                .build();

        //Assert
        fail("Expected a NullPointerException for missing mandatory field");
    }

    @Test
    public void testInlinePictureImplMustHaveTemplateName() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        InlinePictureImpl.builder()
                .file(mockFile)
                .imageType(JPEG)
                .build();

        //Assert
        fail("Expected a NullPointerException for missing mandatory field");
    }

    @Test
    public void testInlinePictureImplValidWithRequiredFields() throws Exception {
        //Arrange

        //Act
        final InlinePictureImpl inlinePicture = InlinePictureImpl.builder()
                .file(mockFile)
                .imageType(JPEG)
                .templateName("template.ftl")
                .build();

        //Assert
        assertThat(inlinePicture, not(is(nullValue())));
    }

}