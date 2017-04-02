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

package it.ozimov.springboot.mail.logging.defaultimpl;

import it.ozimov.springboot.mail.logging.EmailLogRenderer;
import it.ozimov.springboot.mail.logging.EmailRenderer;
import it.ozimov.springboot.mail.model.Email;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class SanitizedEmailLogRendererTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @MockBean
    private EmailRenderer emailRenderer;

    @SpyBean
    private SanitizedEmailLogRenderer emailLogRenderer;

    @Mock
    private Logger customLogger;

    @Mock
    private Email email;

    @Captor
    ArgumentCaptor<String> sanitizedEmailArgumentCaptor;

    @Test
    public void shouldRegisterLoggerReturnThis() throws Exception {
        //Arrange

        //Act
        EmailLogRenderer givenEmailLogRendered = emailLogRenderer.registerLogger(customLogger);

        //Assert
        assertions.assertThat(givenEmailLogRendered).isEqualTo(emailLogRenderer);
    }

    @Test
    public void shouldRegisterLoggerOverrideDefaultLogger() throws Exception {
        //Arrange
        Logger initialLogger = emailLogRenderer.logger;

        //Act
        emailLogRenderer.registerLogger(customLogger);
        Logger givenLogger = emailLogRenderer.logger;

        //Assert
        assertions.assertThat(initialLogger).isNotEqualTo(customLogger);
        assertions.assertThat(givenLogger).isEqualTo(customLogger);
    }

    //---------------------------------------------------
    //--- TRACE
    //---------------------------------------------------
    //---------------------------------------------------

    @Test
    public void shouldTraceEmailNotLogWhenTraceIsDisabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isTraceEnabled()).thenReturn(false);

        //Act
        emailLogRenderer.trace(email);

        //Assert
        verify(customLogger, never()).trace(anyString());
    }

    @Test
    public void shouldTraceEmailWithMessageNotLogWhenTraceIsDisabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isTraceEnabled()).thenReturn(false);
        String messageTemplate = "Email is {} and I add a param here {} and another here {}.";
        int param1 = 123;
        Boolean param2 = true;

        //Act
        emailLogRenderer.trace(messageTemplate, email, param1, param2);

        //Assert
        verify(customLogger, never()).trace(anyString(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    public void shouldTraceEmailLogWhenTraceIsEnabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isTraceEnabled()).thenReturn(true);
        String givenRenderedEmail = "rendered\r \tEmail\n\b";
        String expectedMessage = "rendered\\r \\tEmail\\n\\b";
        when(emailRenderer.render(email)).thenReturn(givenRenderedEmail);

        //Act
        emailLogRenderer.trace(email);

        //Assert
        verify(customLogger).trace(expectedMessage);
    }

    @Test
    public void shouldTraceEmailWithMessageLogWhenTraceIsEnabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isTraceEnabled()).thenReturn(true);
        String givenRenderedEmail = "rendered\r \tEmail\n\b";
        String givenSanitizedEmail = "rendered\\r \\tEmail\\n\\b";
        when(emailRenderer.render(email)).thenReturn(givenRenderedEmail);
        String messageTemplate = "Email is {} and I add a param here {} and another here {}.";
        int param1 = 123;
        Boolean param2 = true;
        String expectedLogMessage = "Email is rendered\\r \\tEmail\\n\\b and I add a param here 123 and another here true";

        //Act
        emailLogRenderer.trace(messageTemplate, email, param1, param2);

        //Assert
        verify(customLogger).trace(messageTemplate, givenSanitizedEmail, new Object[]{param1, param2});
    }

    //---------------------------------------------------
    //--- DEBUG
    //---------------------------------------------------
    //---------------------------------------------------

    @Test
    public void shouldDebugEmailNotLogWhenDebugIsDisabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isDebugEnabled()).thenReturn(false);

        //Act
        emailLogRenderer.debug(email);

        //Assert
        verify(customLogger, never()).debug(anyString());
    }

    @Test
    public void shouldDebugEmailWithMessageNotLogWhenDebugIsDisabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isDebugEnabled()).thenReturn(false);
        String messageTemplate = "Email is {} and I add a param here {} and another here {}.";
        int param1 = 123;
        Boolean param2 = true;

        //Act
        emailLogRenderer.debug(messageTemplate, email, param1, param2);

        //Assert
        verify(customLogger, never()).debug(anyString(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    public void shouldDebugEmailLogWhenDebugIsEnabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isDebugEnabled()).thenReturn(true);
        String givenRenderedEmail = "rendered\r \tEmail\n\b";
        String expectedMessage = "rendered\\r \\tEmail\\n\\b";
        when(emailRenderer.render(email)).thenReturn(givenRenderedEmail);

        //Act
        emailLogRenderer.debug(email);

        //Assert
        verify(customLogger).debug(expectedMessage);
    }

    @Test
    public void shouldDebugEmailWithMessageLogWhenDebugIsEnabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isDebugEnabled()).thenReturn(true);
        String givenRenderedEmail = "rendered\r \tEmail\n\b";
        String givenSanitizedEmail = "rendered\\r \\tEmail\\n\\b";
        when(emailRenderer.render(email)).thenReturn(givenRenderedEmail);
        String messageTemplate = "Email is {} and I add a param here {} and another here {}.";
        int param1 = 123;
        Boolean param2 = true;

        //Act
        emailLogRenderer.debug(messageTemplate, email, param1, param2);

        //Assert
        verify(customLogger).debug(messageTemplate, givenSanitizedEmail, new Object[]{param1, param2});
    }

    //---------------------------------------------------
    //--- INFO
    //---------------------------------------------------
    //---------------------------------------------------

    @Test
    public void shouldInfoEmailNotLogWhenInfoIsDisabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isInfoEnabled()).thenReturn(false);

        //Act
        emailLogRenderer.info(email);

        //Assert
        verify(customLogger, never()).info(anyString());
    }

    @Test
    public void shouldInfoEmailWithMessageNotLogWhenInfoIsDisabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isInfoEnabled()).thenReturn(false);
        String messageTemplate = "Email is {} and I add a param here {} and another here {}.";
        int param1 = 123;
        Boolean param2 = true;

        //Act
        emailLogRenderer.info(messageTemplate, email, param1, param2);

        //Assert
        verify(customLogger, never()).info(anyString(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    public void shouldInfoEmailLogWhenInfoIsEnabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isInfoEnabled()).thenReturn(true);
        String givenRenderedEmail = "rendered\r \tEmail\n\b";
        String expectedMessage = "rendered\\r \\tEmail\\n\\b";
        when(emailRenderer.render(email)).thenReturn(givenRenderedEmail);

        //Act
        emailLogRenderer.info(email);

        //Assert
        verify(customLogger).info(expectedMessage);
    }

    @Test
    public void shouldInfoEmailWithMessageLogWhenInfoIsEnabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isInfoEnabled()).thenReturn(true);
        String givenRenderedEmail = "rendered\r \tEmail\n\b";
        String givenSanitizedEmail = "rendered\\r \\tEmail\\n\\b";
        when(emailRenderer.render(email)).thenReturn(givenRenderedEmail);
        String messageTemplate = "Email is {} and I add a param here {} and another here {}.";
        int param1 = 123;
        Boolean param2 = true;

        //Act
        emailLogRenderer.info(messageTemplate, email, param1, param2);

        //Assert
        verify(customLogger).info(messageTemplate, givenSanitizedEmail, new Object[]{param1, param2});
    }

    //---------------------------------------------------
    //--- WARN
    //---------------------------------------------------
    //---------------------------------------------------

    @Test
    public void shouldWarnEmailNotLogWhenWarnIsDisabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isWarnEnabled()).thenReturn(false);

        //Act
        emailLogRenderer.warn(email);

        //Assert
        verify(customLogger, never()).warn(anyString());
    }

    @Test
    public void shouldWarnEmailWithMessageNotLogWhenWarnIsDisabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isWarnEnabled()).thenReturn(false);
        String messageTemplate = "Email is {} and I add a param here {} and another here {}.";
        int param1 = 123;
        Boolean param2 = true;

        //Act
        emailLogRenderer.warn(messageTemplate, email, param1, param2);

        //Assert
        verify(customLogger, never()).warn(anyString(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    public void shouldWarnEmailLogWhenWarnIsEnabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isWarnEnabled()).thenReturn(true);
        String givenRenderedEmail = "rendered\r \tEmail\n\b";
        String expectedMessage = "rendered\\r \\tEmail\\n\\b";
        when(emailRenderer.render(email)).thenReturn(givenRenderedEmail);

        //Act
        emailLogRenderer.warn(email);

        //Assert
        verify(customLogger).warn(expectedMessage);
    }

    @Test
    public void shouldWarnEmailWithMessageLogWhenWarnIsEnabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isWarnEnabled()).thenReturn(true);
        String givenRenderedEmail = "rendered\r \tEmail\n\b";
        String givenSanitizedEmail = "rendered\\r \\tEmail\\n\\b";
        when(emailRenderer.render(email)).thenReturn(givenRenderedEmail);
        String messageTemplate = "Email is {} and I add a param here {} and another here {}.";
        int param1 = 123;
        Boolean param2 = true;

        //Act
        emailLogRenderer.warn(messageTemplate, email, param1, param2);

        //Assert
        verify(customLogger).warn(messageTemplate, givenSanitizedEmail, new Object[]{param1, param2});
    }

    //---------------------------------------------------
    //--- ERROR
    //---------------------------------------------------
    //---------------------------------------------------

    @Test
    public void shouldErrorEmailNotLogWhenErrorIsDisabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isErrorEnabled()).thenReturn(false);

        //Act
        emailLogRenderer.error(email);

        //Assert
        verify(customLogger, never()).error(anyString());
    }

    @Test
    public void shouldErrorEmailWithMessageNotLogWhenErrorIsDisabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isErrorEnabled()).thenReturn(false);
        String messageTemplate = "Email is {} and I add a param here {} and another here {}.";
        int param1 = 123;
        Boolean param2 = true;

        //Act
        emailLogRenderer.error(messageTemplate, email, param1, param2);

        //Assert
        verify(customLogger, never()).error(anyString(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    public void shouldErrorEmailLogWhenErrorIsEnabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isErrorEnabled()).thenReturn(true);
        String givenRenderedEmail = "rendered\r \tEmail\n\b";
        String expectedMessage = "rendered\\r \\tEmail\\n\\b";
        when(emailRenderer.render(email)).thenReturn(givenRenderedEmail);

        //Act
        emailLogRenderer.error(email);

        //Assert
        verify(customLogger).error(expectedMessage);
    }

    @Test
    public void shouldErrorEmailWithMessageLogWhenErrorIsEnabled() throws Exception {
        //Arrange
        emailLogRenderer.registerLogger(customLogger);
        when(customLogger.isErrorEnabled()).thenReturn(true);
        String givenRenderedEmail = "rendered\r \tEmail\n\b";
        String givenSanitizedEmail = "rendered\\r \\tEmail\\n\\b";
        when(emailRenderer.render(email)).thenReturn(givenRenderedEmail);
        String messageTemplate = "Email is {} and I add a param here {} and another here {}.";
        int param1 = 123;
        Boolean param2 = true;

        //Act
        emailLogRenderer.error(messageTemplate, email, param1, param2);

        //Assert
        verify(customLogger).error(messageTemplate, givenSanitizedEmail, new Object[]{param1, param2});
    }

}