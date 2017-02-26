package it.ozimov.springboot.mail.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ImageTypeTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    private ImageType imageType;

    public ImageTypeTest(ImageType imageType) {
        this.imageType = imageType;
    }

    @Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> params = new ArrayList<>();
        for (ImageType imageType : ImageType.values()) {
            params.add(new Object[]{imageType});
        }
        return params;
    }


    @Test
    public void shouldGetExtension() throws Exception {
        //Act
        String givenExtension = imageType.getExtension();

        //Assert
        assertions.assertThat(givenExtension)
                .isNotNull()
                .isNotEmpty()
                .matches("^[a-z]+$");
    }

    @Test
    public void shouldGetContentType() throws Exception {
        //Act
        String givenContentType = imageType.getContentType();

        //Assert
        assertions.assertThat(givenContentType)
                .isNotNull()
                .isNotEmpty()
                .matches("^[a-z]+/[a-z]+$");
    }

}