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

package it.ozimov.springboot.mail.service.defaultimpl;

import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;

public class TemplatingTestUtils {

    public static final String TEMPLATE = "email_template.ftl";
    public static final String NAME = "Titus";
    public static final Map<String, Object> MODEL_OBJECT = new ImmutableMap.Builder<String, Object>()
            .put("name", NAME)
            .build();

    public static String getExpectedBody() throws IOException {
        final File file = new File(TemplatingTestUtils.class.getClassLoader()
                .getResource("templates" + File.separator + TEMPLATE).getFile());
        final String template = readFile(file);
        return template.replace("${name}", NAME);
    }

    private static String readFile(final File file) throws IOException {
        final byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, Charset.forName("UTF-8"));
    }

}