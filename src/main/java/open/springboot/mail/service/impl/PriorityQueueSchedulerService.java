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

package open.springboot.mail.service.impl;

import lombok.extern.slf4j.Slf4j;
import open.springboot.mail.model.Email;
import open.springboot.mail.model.EmailSchedulingWrapper;
import open.springboot.mail.model.EmailTemplateSchedulingWrapper;
import open.springboot.mail.model.InlinePicture;
import open.springboot.mail.service.EmailService;
import open.springboot.mail.service.Exception.CannotSendEmailException;
import open.springboot.mail.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.Map;
import java.util.TreeSet;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@Slf4j
public class PriorityQueueSchedulerService implements SchedulerService {

    /**
     * millisecs elapsed form the call of the send method and the actual sending by SMTP server
     */
    private static final long DELTA = SECONDS.toMillis(1);

    private Long timeOfNextScheduledMessage;

    private TreeSet<EmailSchedulingWrapper>[] queues;

    private EmailService emailService;

    private Consumer consumer;

    @Autowired
    private PriorityQueueSchedulerService(
            final EmailService emailService,
            @Value("${spring.mail.scheduler.priorityLevels ?: 10}") final int numberOfPriorirtyLevels) {
        this.emailService = emailService;
        queues = new TreeSet[numberOfPriorirtyLevels];
        for (int i = 0; i < numberOfPriorirtyLevels; i++) {
            queues[i] = new TreeSet<>();
        }
        consumer = new Consumer();
        consumer.start();
    }

    @Override
    public synchronized void schedule(final Email mimeEmail, final Date scheduledDate, final int priorityLevel) {
        //the priority level must be between 0 and numberOfPriorirtyLevels-1
        final int realPriorityLevel = max(0, min(priorityLevel, queues.length - 1));
        final EmailSchedulingWrapper esw = new EmailSchedulingWrapper(mimeEmail, scheduledDate, realPriorityLevel);
        queues[priorityLevel].add(esw);
        if (isNull(timeOfNextScheduledMessage) || scheduledDate.getTime()<timeOfNextScheduledMessage) {
            notify();
        }
    }

    @Override
    public synchronized void schedule(final Email mimeEmail, final String template, final Map<String, Object> modelObject,
                                      final Date scheduledDate, final int priorityLevel,
                                      final InlinePicture... inlinePictures) throws CannotSendEmailException {
        final int realPriorityLevel = max(0, min(priorityLevel, queues.length - 1));
        final EmailTemplateSchedulingWrapper esw = new EmailTemplateSchedulingWrapper(mimeEmail, scheduledDate, realPriorityLevel,
                template, modelObject, inlinePictures);
        queues[priorityLevel].add(esw);
        if (isNull(timeOfNextScheduledMessage) || scheduledDate.getTime()<timeOfNextScheduledMessage) {
            notify();
        }
    }

    private synchronized EmailSchedulingWrapper dequeue() throws InterruptedException {
        EmailSchedulingWrapper esw = null;
        timeOfNextScheduledMessage = null;
        while (isNull(esw)) {
            //try to find a message in queue
            final long now = System.currentTimeMillis();
            for (TreeSet<EmailSchedulingWrapper> q : queues) {
                if (!q.isEmpty()) {
                    long time = q.first().getScheduledDate().getTime();
                    if (time - now <= DELTA) {
                        //message found!
                        esw = q.pollFirst();
                        break;
                    } else {
                        if (isNull(timeOfNextScheduledMessage) || time < timeOfNextScheduledMessage) {
                            timeOfNextScheduledMessage = time;
                        }
                    }
                }
            }
            if (isNull(esw)) {
                //no message was found, let's sleep, some message may arrive in the meanwhile
                if (timeOfNextScheduledMessage==null) { //all the queues are empty
                    wait(); //wait for a new email to be scheduled
                } else {
                    final long waitTime=timeOfNextScheduledMessage - System.currentTimeMillis()-DELTA;
                    if (waitTime>0) {
                        wait(waitTime); //wait before sending the most imminent scheduled email
                    }
                }
            }
        }
        //here esw is the message to send
        return esw;
    }

    @PreDestroy
    private void cleanUp() throws Exception {
        consumer.close();
    }

    class Consumer extends Thread {

        private boolean canRun;

        public void run() {
            log.info("Email scheduler consumer started");
            while (canRun) {
                try {
                    final EmailSchedulingWrapper esw = dequeue();
                    if (esw instanceof EmailTemplateSchedulingWrapper) {
                        final EmailTemplateSchedulingWrapper etsw = (EmailTemplateSchedulingWrapper) esw;
                        try {
                            emailService.send(etsw.getEmail(), etsw.getTemplate(), etsw.getModelObject(), etsw.getInlinePictures());
                        } catch (CannotSendEmailException e) {
                           log.error("An error occurred while sending the email", e);
                        }
                    } else {
                        emailService.send(esw.getEmail());
                    }
                } catch (final InterruptedException e) {
                    log.error("Email scheduler consumer interrupted", e);
                }
            }
            log.info("Email scheduler consumer stopped");
        }

        public void close() {
            canRun = false;
        }

    }
}
