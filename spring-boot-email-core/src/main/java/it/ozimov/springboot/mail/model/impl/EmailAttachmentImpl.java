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

package it.ozimov.springboot.mail.model.impl;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import it.ozimov.springboot.mail.model.EmailAttachment;
import it.ozimov.springboot.mail.utils.TikaDetector;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Optional.ofNullable;

/**
 * Plain text email.
 *
 * @author rtrunfio
 */
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "attachmentData")
@EqualsAndHashCode
@Slf4j
public class EmailAttachmentImpl implements EmailAttachment {

    private static final long serialVersionUID = -3307831714212032363L;

    @Getter
    private
    @NonNull
    String attachmentName;

    @Getter
    private
    @NonNull
    byte[] attachmentData;

    private MediaType mediaType;

    public ByteArrayResource getInputStream() {
        return new ByteArrayResource(attachmentData);
    }

    public MediaType getContentType() throws IOException {
        final InputStream attachmentDataStream = new ByteArrayInputStream(attachmentData);

        final MediaType mediaType;
        try {
            mediaType = ofNullable(this.mediaType)
                    .orElse(TikaDetector.tikaDetector().detect(attachmentDataStream, attachmentName));
        } catch (IOException e) {
            log.error("The MimeType is not set. Tried to guess it but something went wrong.", e);
            throw e;
        }
        return mediaType;
    }

}