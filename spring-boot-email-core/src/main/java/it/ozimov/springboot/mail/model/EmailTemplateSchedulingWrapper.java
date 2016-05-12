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

package it.ozimov.springboot.mail.model;

import lombok.Getter;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.Map;


@Getter
public class EmailTemplateSchedulingWrapper extends EmailSchedulingWrapper {

    private final String template;
    private final Map<String, Object> modelObject;
    private final InlinePicture[] inlinePictures;

    public EmailTemplateSchedulingWrapper(@NonNull final Email email,
                                          @NonNull final OffsetDateTime scheduledDateTime,
                                          final int priority,
                                          @NonNull final String template,
                                          @NonNull final Map<String, Object> modelObject,
                                          @NonNull final InlinePicture[] inlinePictures) {
        super(email, scheduledDateTime, priority);
        this.template = template;
        this.modelObject = modelObject;
        this.inlinePictures = inlinePictures;
    }

}
