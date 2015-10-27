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

package open.springboot.mail.service;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;

/**
 * Defines a service for processing templates with a template engine
 */
public interface TemplateService {

    /**
     * Call the template engine to process the given template with the given model object.
     *
     * @param template a template file to be processed
     * @param model the model object to process the template
     * @return a processed templated (an HTML, or XML, or wathever the template engine can process)
     * @throws IOException thrown if the template file is not found or cannot be accessed
     * @throws TemplateException if the template cannot be processed with the given model object
     */
    String mergeTemplateIntoString(String template, Map<String, Object> model)
            throws IOException, TemplateException;


}
