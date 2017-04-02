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

package it.ozimov.springboot.mail.logging;

import it.ozimov.springboot.mail.model.Email;
import org.slf4j.Logger;

public interface EmailLogRenderer {

    /**
     * Register a logger to be used in alternative to the one from the class.
     * <p>
     *
     * @param logger The logger to be used.
     */
    EmailLogRenderer registerLogger(Logger logger);

    /**
     * Log a trace message.
     * <p>
     * If the logger is currently enabled for the trace message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param message       The string message (or a key in the message catalog)
     * @param email         Email to be logged
     * @param messageParams Additional objects to be injected in the message template
     */
    void trace(String message, Email email, Object... messageParams);

    /**
     * Log a trace message representing the given email.
     * <p>
     * If the logger is currently enabled for the trace message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param email Email to be logged
     */
    void trace(Email email);

    /**
     * Log a debug message.
     * <p>
     * If the logger is currently enabled for the debug message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param message       The string message (or a key in the message catalog)
     * @param email         Email to be logged
     * @param messageParams Additional objects to be injected in the message template
     */
    void debug(String message, Email email, Object... messageParams);

    /**
     * Log a debug message representing the given email.
     * <p>
     * If the logger is currently enabled for the debug message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param email Email to be logged
     */
    void debug(Email email);

    /**
     * Log an info message.
     * <p>
     * If the logger is currently enabled for the info message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param message       The string message (or a key in the message catalog)
     * @param email         Email to be logged
     * @param messageParams Additional objects to be injected in the message template
     */
    void info(String message, Email email, Object... messageParams);

    /**
     * Log an info message representing the given email.
     * <p>
     * If the logger is currently enabled for the info message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param email Email to be logged
     */
    void info(Email email);

    /**
     * Log a warning message.
     * <p>
     * If the logger is currently enabled for the warning message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param message       The string message (or a key in the message catalog)
     * @param email         Email to be logged
     * @param messageParams Additional objects to be injected in the message template
     */
    void warn(String message, Email email, Object... messageParams);

    /**
     * Log a warning message representing the given email.
     * <p>
     * If the logger is currently enabled for the warning message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param email Email to be logged
     */
    void warn(Email email);

    /**
     * Log a error message.
     * <p>
     * If the logger is currently enabled for the error message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param message       The string message (or a key in the message catalog)
     * @param email         Email to be logged
     * @param messageParams Additional objects to be injected in the message template
     */
    void error(String message, Email email, Object... messageParams);

    /**
     * Log an error message representing the given email.
     * <p>
     * If the logger is currently enabled for the error message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param email Email to be logged
     */
    void error(Email email);

}