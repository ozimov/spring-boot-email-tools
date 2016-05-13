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

package it.ozimov.springboot.templating.mail.model.impl;

import it.ozimov.springboot.templating.mail.model.ImageType;
import it.ozimov.springboot.templating.mail.model.InlinePicture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.File;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InlinePictureImpl implements InlinePicture {

    private
    @NonNull
    ImageType imageType;

    private
    @NonNull
    File file;

    private
    @NonNull
    String templateName;

}
