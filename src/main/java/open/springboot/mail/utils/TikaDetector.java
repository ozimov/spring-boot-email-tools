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

package open.springboot.mail.utils;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.tika.metadata.TikaMetadataKeys.RESOURCE_NAME_KEY;

/**
 * <p>
 * A convenience singleton class that uses ApacheTika to guess the MIME type.
 * </p>
 */
public class TikaDetector {

    private Detector detector;

    private enum Singleton {
        INSTANCE;

        final TikaDetector singleton = new TikaDetector();
    }

    public static TikaDetector tikaDetector() {
        return Singleton.INSTANCE.singleton;
    }

    public TikaDetector() {
        final TikaConfig config = TikaConfig.getDefaultConfig();
        detector = config.getDetector();
    }

    /**
     * Detect the MediaType tikaDetector the given input stream
     *
     * @param inputStream the stream for which the content has to be detected
     * @param fileName    filename with extension
     * @return the guessed media type
     */
    public org.springframework.http.MediaType detect(final InputStream inputStream, final String fileName) throws IOException {
        final TikaInputStream stream = TikaInputStream.get(inputStream);
        return detect(stream, fileName);
    }

    /**
     * Detect the MediaType tikaDetector the given input stream
     *
     * @param file the file for which the content has to be detected
     * @return the guessed media type
     */
    public org.springframework.http.MediaType detect(final File file) throws IOException {
        checkNotNull(file);
        checkArgument(file.exists(), "The given File object does not exists");
        checkArgument(file.isFile(), "The given File object does not represent a file");

        final TikaInputStream stream = TikaInputStream.get(file);
        return detect(stream, file.getName());
    }

    private org.springframework.http.MediaType detect(final TikaInputStream stream, final String fileName) throws IOException {
        final Metadata metadata = new Metadata();
        metadata.add(RESOURCE_NAME_KEY, fileName);
        final MediaType mediaType = detector.detect(stream, metadata);
        return toSpringMediaType(mediaType);
    }

    private static org.springframework.http.MediaType toSpringMediaType(final MediaType mediaType) {
        return org.springframework.http.MediaType.valueOf(mediaType.getType());
    }

}
