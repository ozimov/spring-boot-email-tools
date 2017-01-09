package it.ozimov.springboot.templating.mail.utils;

import org.junit.Test;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.InputStream;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static org.hamcrest.Matchers.is;
import static testutils.TestUtils.loadFileIntoInputStream;

public class TikaDetectorTest {

    private static final String FILE_NAME = "100_percent_free.jpg";
    private static final String FILE_PATH = "images" + File.separator + FILE_NAME;

    @Test
    public void testCanGetAlwaysSameTikaDetectorInstance() throws Exception {
        //Act
        final TikaDetector tikaDetector1 = TikaDetector.tikaDetector();
        final TikaDetector tikaDetector2 = TikaDetector.tikaDetector();

        //Assert
        given(tikaDetector1).assertThat(is(tikaDetector2));
    }

    @Test
    public void testDetect() throws Exception {
        //Arrange
        final InputStream inputStream = loadFileIntoInputStream(FILE_PATH);

        //Act
        final MediaType mediaType = TikaDetector.tikaDetector().detect(inputStream, FILE_NAME);

        //Assert
        given(mediaType).assertThat(is(MediaType.IMAGE_JPEG));
    }

}