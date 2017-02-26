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

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;
import static org.apache.tika.metadata.TikaMetadataKeys.RESOURCE_NAME_KEY;

/**
 * A convenience singleton class that uses ApacheTika to guess the MIME type.
 */
public class TikaDetector {

    private final Detector detector;

    private TikaDetector() {
        final TikaConfig config = TikaConfig.getDefaultConfig();
        detector = config.getDetector();
    }

    public static TikaDetector tikaDetector() {
        return TikaDetectorSingletonHolder.TIKA_DETECTOR;
    }

    private static org.springframework.http.MediaType toSpringMediaType(final MediaType mediaType) {
        return org.springframework.http.MediaType.valueOf(mediaType.toString());
    }

    /**
     * Detect the MediaType of the given input stream
     *
     * @param inputStream the stream for which the content has to be detected
     * @param fileName    filename with extension
     * @return the guessed media type
     */
    public org.springframework.http.MediaType detect(final InputStream inputStream, final String fileName) throws IOException {
        final TikaInputStream stream = TikaInputStream.get(inputStream);
        return detect(stream, fileName);
    }

    private org.springframework.http.MediaType detect(final TikaInputStream stream, final String fileName) throws IOException {
        final Metadata metadata = new Metadata();
        metadata.add(RESOURCE_NAME_KEY, fileName);
        final MediaType mediaType = detector.detect(requireNonNull(stream), metadata);
        return toSpringMediaType(mediaType);
    }

    private static class TikaDetectorSingletonHolder {
        public static final TikaDetector TIKA_DETECTOR = new TikaDetector();
    }

}
