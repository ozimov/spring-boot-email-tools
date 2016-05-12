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

package testutils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class TestUtils {

    private TestUtils() {
    }

    public static File loadFile(final String path) {
        return new File(TestUtils.class.getClassLoader()
                .getResource(path).getFile());
    }

    public static byte[] loadFileIntoByte(final String path) throws IOException {
        return Files.readAllBytes(loadFile(path).toPath());
    }

    public static String readFile(final String path) throws IOException {
        return new String(loadFileIntoByte(path), Charset.forName("UTF-8"));
    }

}