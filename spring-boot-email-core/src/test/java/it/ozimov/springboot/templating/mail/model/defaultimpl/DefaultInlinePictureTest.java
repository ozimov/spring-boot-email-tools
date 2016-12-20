package it.ozimov.springboot.templating.mail.model.defaultimpl;

import it.ozimov.springboot.templating.mail.UnitTest;
import it.ozimov.springboot.templating.mail.model.ImageType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class DefaultInlinePictureTest implements UnitTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private File mockFile;

    @Test
    public void testInlinePictureImplMustHaveImageType() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        DefaultInlinePicture.builder()
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
        DefaultInlinePicture.builder()
                .imageType(ImageType.JPEG)
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
        DefaultInlinePicture.builder()
                .file(mockFile)
                .imageType(ImageType.JPEG)
                .build();

        //Assert
        fail("Expected a NullPointerException for missing mandatory field");
    }

    @Test
    public void testInlinePictureImplValidWithRequiredFields() throws Exception {
        //Arrange

        //Act
        final DefaultInlinePicture inlinePicture = DefaultInlinePicture.builder()
                .file(mockFile)
                .imageType(ImageType.JPEG)
                .templateName("template.ftl")
                .build();

        //Assert
        assertThat(inlinePicture, not(is(nullValue())));
    }

}