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

package open.springboot.mail.advicetraits;

import open.springboot.mail.exceptions.EmailConversionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.AdviceTrait;
import org.zalando.problem.spring.web.advice.Responses;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * Definition of the advice trait for the {@linkplain EmailConversionException}.
 * It must be used in combination with a class annotated with
 * {@linkplain org.springframework.web.bind.annotation.ControllerAdvice}, e.g.
 *
 * <code>
 *     @ControllerAdvice
 *      class ExceptionHandling implements EmailConversionAdviceTrait {
 *
 *      }
 * </code>
 *
 * @see <a href="https://github.com/zalando/problem-spring-web/blob/master/README.md">problem-spring-web</a>
 * official page on <em>GitHub</em>.
 *
 */
public interface EmailConversionAdviceTrait extends AdviceTrait {

    @ExceptionHandler
    default ResponseEntity<Problem> handleMediaTypeNotAcceptable(
            final EmailConversionException exception,
            final NativeWebRequest request) {
        return Responses.create(INTERNAL_SERVER_ERROR, exception, request);
    }

}