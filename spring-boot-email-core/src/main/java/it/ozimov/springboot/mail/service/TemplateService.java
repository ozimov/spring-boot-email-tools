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

package it.ozimov.springboot.mail.service;

import it.ozimov.springboot.mail.service.exception.TemplateException;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;

/**
 * Defines a service for processing templates with a template engine
 */
public interface TemplateService {

    /**
     * Call the templateReference engine to process the given templateReference with the given model object.
     *
     * @param templateReference a templateReference file to be processed. The name must be complaint with the position
     *                          of the template your your resources folder. Usually, files in {@code resources/templates}
     *                          are resolved by passing the sole file name. Subfolders of {@code resources/templates} must
     *                          be explicitly reported. E.g., a template {@code template.html} under the folder
     *                          {@code resources/templates/module} must be reported as {@code "module/template"}
     * @param model             the model object to process the templateReference
     * @return a processed template (an HTML, or XML, or whatever the templateReference engine can process)
     * @throws IOException       thrown if the templateReference file is not found or cannot be accessed
     * @throws TemplateException if the templateReference cannot be processed with the given model object
     */
    String mergeTemplateIntoString(String templateReference, Map<String, Object> model)
            throws IOException, TemplateException;


    /**
     * Return the expected template file extension. The String must not start with '.'.
     *
     * @return The expected extension of thr accepted template file
     */
    String expectedTemplateExtension();

}
