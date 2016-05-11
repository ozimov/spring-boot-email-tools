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

import open.springboot.mail.model.Email;
import open.springboot.mail.model.InlinePicture;
import open.springboot.mail.service.Exception.CannotSendEmailException;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;

public interface SchedulerService {

    /**
     * Schedules the sending of an email message.
     *
     * @param mimeEmail     an email to be sent
     * @param scheduledDateTime the date-time at which the email should be sent
     * @param priorityLevel the priority level for the email:
     *                      the emails with scheduledTime<=now are sent according to an order depending
     *                      on their priority level
     */
    void schedule(Email mimeEmail, OffsetDateTime scheduledDateTime, int priorityLevel);

    /**
     * Schedules the sending of an email message.
     *
     * @param mimeEmail      an email to be sent
     * @param scheduledDateTime  the date-time at which the email should be sent
     * @param priorityLevel  the priority level for the email:
     *                       the emails with scheduledTime<=now are sent according to an order depending
     *                       on their priority level
     * @param template       the reference to the template file
     * @param modelObject    the model object to be used for the template engine, it may be null
     * @param inlinePictures list of pictures to be rendered inline in the template
     */
    void schedule(Email mimeEmail,
                  OffsetDateTime scheduledDateTime, int priorityLevel,
                  String template, Map<String, Object> modelObject,
                  InlinePicture... inlinePictures) throws CannotSendEmailException;

}
