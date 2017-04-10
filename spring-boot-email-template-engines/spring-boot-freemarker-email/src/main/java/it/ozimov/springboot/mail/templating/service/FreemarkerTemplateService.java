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

package it.ozimov.springboot.mail.templating.service;

import freemarker.template.Configuration;
import it.ozimov.springboot.mail.service.TemplateService;
import it.ozimov.springboot.mail.service.exception.TemplateException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Files.getFileExtension;

@Service
public class FreemarkerTemplateService implements TemplateService {

    @Autowired
    private Configuration freemarkerConfiguration;

    @Override
    @NonNull
    public String mergeTemplateIntoString(final @NonNull String templateReference,
                                          final @NonNull Map<String, Object> model)
            throws IOException, TemplateException {
        final String trimmedTemplateReference = templateReference.trim();
        checkArgument(!isNullOrEmpty(trimmedTemplateReference), "The given template is null, empty or blank");
        if (trimmedTemplateReference.contains("."))
            checkArgument(Objects.equals(getFileExtension(trimmedTemplateReference), expectedTemplateExtension()),
                    "Expected a Freemarker template file with extension 'ftl', while '%s' was given",
                    getFileExtension(trimmedTemplateReference));

        try {
            final String normalizedTemplateReference = trimmedTemplateReference.endsWith(expectedTemplateExtension()) ?
                    trimmedTemplateReference : trimmedTemplateReference + '.' + expectedTemplateExtension();
            return FreeMarkerTemplateUtils.processTemplateIntoString(
                    freemarkerConfiguration.getTemplate(normalizedTemplateReference, Charset.forName("UTF-8").name()), model);
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }

    @Override
    public String expectedTemplateExtension() {
        return "ftl";
    }

}