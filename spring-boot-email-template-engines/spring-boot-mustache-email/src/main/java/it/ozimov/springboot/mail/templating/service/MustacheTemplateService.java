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

import it.ozimov.springboot.mail.service.TemplateService;
import it.ozimov.springboot.mail.service.exception.TemplateException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Files.getFileExtension;

@Service("templateService")
public class MustacheTemplateService implements TemplateService {

    @Autowired
    private MustacheAutoConfiguration mustacheAutoConfiguration;

    @Value("${spring.mustache.suffix:.html}")
    private String mustacheSuffix;

    @Override
    public
    @NonNull
    String mergeTemplateIntoString(final @NonNull String templateReference,
                                   final @NonNull Map<String, Object> model)
            throws IOException, TemplateException {
        final String trimmedTemplateReference = templateReference.trim();
        checkArgument(!isNullOrEmpty(trimmedTemplateReference), "The given templateName is null, empty or blank");
        if (trimmedTemplateReference.contains("."))
            checkArgument(Objects.equals(getFileExtension(trimmedTemplateReference), expectedTemplateExtension()),
                    "Expected a Mustache template file with extension '%s', while '%s' was given. To check " +
                            "the default extension look at 'spring.mustache.suffix' in your application.properties file",
                    expectedTemplateExtension(), getFileExtension(trimmedTemplateReference));


        try {
            final Reader template = mustacheAutoConfiguration.mustacheTemplateLoader()
                    .getTemplate(normalizeTemplateReference(trimmedTemplateReference));

            return mustacheAutoConfiguration.mustacheCompiler(mustacheAutoConfiguration.mustacheTemplateLoader())
                    .compile(template)
                    .execute(model);
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }

    @Override
    public String expectedTemplateExtension() {
        return mustacheSuffix.replace(".", "");
    }

    private String normalizeTemplateReference(final String templateReference) {
        if (templateReference.endsWith(mustacheSuffix)) {
            final String expectedSuffix = ("." + mustacheSuffix).replace("..", ".");
            return templateReference.substring(0, templateReference.lastIndexOf(expectedSuffix));
        } else {
            return templateReference;
        }
    }

}