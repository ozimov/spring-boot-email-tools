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

package it.ozimov.springboot.mail.utils;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.InputStream;

import static testutils.TestUtils.loadFileIntoInputStream;

public class TikaDetectorTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    private static final String FILE_NAME = "100_percent_free.jpg";
    private static final String FILE_PATH = "images" + File.separator + FILE_NAME;

    @Test
    public void testCanGetAlwaysSameTikaDetectorInstance() throws Exception {
        //Act
        final TikaDetector tikaDetector1 = TikaDetector.tikaDetector();
        final TikaDetector tikaDetector2 = TikaDetector.tikaDetector();

        //Assert
        assertions.assertThat(tikaDetector1).isEqualTo(tikaDetector2);
    }

    @Test
    public void testDetect() throws Exception {
        //Arrange
        final InputStream inputStream = loadFileIntoInputStream(FILE_PATH);

        //Act
        final MediaType mediaType = TikaDetector.tikaDetector().detect(inputStream, FILE_NAME);

        //Assert
        assertions.assertThat(mediaType).isEqualTo(MediaType.IMAGE_JPEG);
    }

}