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

package it.ozimov.springboot.mail.model.defaultimpl;

import it.ozimov.springboot.mail.UnitTest;
import it.ozimov.springboot.mail.model.ImageType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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