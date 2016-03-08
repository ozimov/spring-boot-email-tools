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

package open.springboot.mail.model;

import lombok.Getter;
import lombok.NonNull;
import open.springboot.mail.model.impl.EmailAttachmentImpl;

import javax.annotation.Generated;
import javax.mail.internet.InternetAddress;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

@Getter
public class EmailSchedulingWrapper implements Comparable<EmailSchedulingWrapper> {

    private final Email email;
    private final Date scheduledDate;
    private final int priority;

    public EmailSchedulingWrapper(@NonNull final Email email, @NonNull final Date scheduledDate, @NonNull final int priority) {
        this.email = email;
        this.scheduledDate = scheduledDate;
        this.priority = priority;
    }

    @Override
    public int compareTo(EmailSchedulingWrapper o) {
        if (scheduledDate.before(o.getScheduledDate())) {
            return -1;
        } else if (scheduledDate.after(o.getScheduledDate())) {
            return 1;
        } else {
            return Integer.compare(priority, o.getPriority());
        }
    }
}
