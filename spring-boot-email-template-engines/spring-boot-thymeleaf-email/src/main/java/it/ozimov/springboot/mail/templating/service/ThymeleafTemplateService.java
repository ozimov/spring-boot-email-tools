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
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Files.getFileExtension;

@Service
public class ThymeleafTemplateService implements TemplateService {

    @Autowired
    private SpringTemplateEngine thymeleafEngine;

    @Value("${spring.thymeleaf.suffix:.html}")
    private String thymeleafSuffix;

    @Override
    public
    @NonNull
    String mergeTemplateIntoString(final @NonNull String templateReference,
                                   final @NonNull Map<String, Object> model)
            throws IOException, TemplateException {
        final String trimmedTemplateReference = templateReference.trim();
        checkArgument(!isNullOrEmpty(trimmedTemplateReference), "The given template is null, empty or blank");
        if (trimmedTemplateReference.contains("."))
            checkArgument(Objects.equals(getNormalizedFileExtension(trimmedTemplateReference), expectedTemplateExtension()),
                    "Expected a Thymeleaf template file with extension '%s', while '%s' was given. To check " +
                            "the default extension look at 'spring.thymeleaf.suffix' in your application.properties file",
                    expectedTemplateExtension(), getNormalizedFileExtension(trimmedTemplateReference));

        final Context context = new Context();
        context.setVariables(model);

        return thymeleafEngine.process(FilenameUtils.removeExtension(trimmedTemplateReference), context);
    }

    @Override
    public String expectedTemplateExtension() {
        return thymeleafSuffix;
    }

    private String getNormalizedFileExtension(final String templateReference) {
        return "." + getFileExtension(templateReference);
    }

}