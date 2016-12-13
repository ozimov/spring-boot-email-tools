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

package it.ozimov.springboot.templating.mail.service.defaultimpl;

import it.ozimov.springboot.templating.mail.model.Email;
import it.ozimov.springboot.templating.mail.model.EmailSchedulingData;
import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.templating.mail.model.defaultimpl.TemplateEmailSchedulingData;
import it.ozimov.springboot.templating.mail.model.InlinePicture;
import it.ozimov.springboot.templating.mail.service.EmailService;
import it.ozimov.springboot.templating.mail.service.PersistenceService;
import it.ozimov.springboot.templating.mail.service.SchedulerService;
import it.ozimov.springboot.templating.mail.service.ServiceStatus;
import it.ozimov.springboot.templating.mail.service.exception.CannotSendEmailException;
import it.ozimov.springboot.templating.mail.utils.TimeUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

//TODO create a scheduler service that compiles the template as soon as possible by balancing the template engine
//load to minimize the response time
@Service
@Slf4j
public class PriorityQueueSchedulerService implements SchedulerService {

    /**
     * millisecs elapsed form the call of the send method and the actual sending by SMTP server
     */
    private static final long DELTA = SECONDS.toMillis(1);
    private ServiceStatus serviceStatus = ServiceStatus.CREATED;
    private Long timeOfNextScheduledMessage;

    private TreeSet<EmailSchedulingData>[] queues;

    private EmailService emailService;

    private Consumer consumer;

    private Optional<PersistenceService> persistenceService;

    @Autowired
    public PriorityQueueSchedulerService(
            final EmailService emailService,
            @Value("${spring.mail.scheduler.priorityLevels:10}") final int numberOfPriorityLevels,
            final Optional<PersistenceService> persistenceServiceOptional) {
        checkArgument(numberOfPriorityLevels > 0, "Expected at least one priority level");
        this.emailService = requireNonNull(emailService);
        this.persistenceService = persistenceServiceOptional;
        queues = new TreeSet[numberOfPriorityLevels];
        for (int i = 0; i < numberOfPriorityLevels; i++) {
            queues[i] = new TreeSet<>();
        }
        consumer = new Consumer();

        synchronized(this) {
            consumer.start();

            //EmailBatch
        }
    }

    @Override
    public synchronized void schedule(@NonNull final Email mimeEmail, @NonNull final OffsetDateTime scheduledDateTime,
                                      final int desiredPriorityLevel) {
        checkPriorityLevel(desiredPriorityLevel);

        final int assignedPriorityLevel = normalizePriority(desiredPriorityLevel);
        final EmailSchedulingData emailSchedulingData = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                                                            .email(mimeEmail)
                                                            .scheduledDateTime(scheduledDateTime)
                                                            .assignedPriority(assignedPriorityLevel)
                                                            .desiredPriority(desiredPriorityLevel)
                                                            .build();
        queues[desiredPriorityLevel - 1].add(emailSchedulingData);
        log.info("Scheduled email {} at UTC time {} with priority {}", mimeEmail, scheduledDateTime, desiredPriorityLevel);
        if (isNull(timeOfNextScheduledMessage) || scheduledDateTime.toInstant().toEpochMilli() < timeOfNextScheduledMessage) {
            notify(); //the consumer, if waiting, is notified and can try to send next scheduled message
        }
    }

    @Override
    public synchronized void schedule(@NonNull final Email mimeEmail,
                                      @NonNull final OffsetDateTime scheduledDateTime,
                                      final int desiredPriorityLevel,
                                      @NonNull final String template,
                                      @NonNull final Map<String, Object> modelObject,
                                      final InlinePicture... inlinePictures) throws CannotSendEmailException {
        checkPriorityLevel(desiredPriorityLevel);

        final int assignedPriorityLevel = normalizePriority(desiredPriorityLevel);
        final EmailSchedulingData emailTemplateSchedulingData = TemplateEmailSchedulingData.templateEmailSchedulingDataBuilder()
                .email(mimeEmail)
                .scheduledDateTime(scheduledDateTime)
                .assignedPriority(assignedPriorityLevel)
                .desiredPriority(desiredPriorityLevel)
                .template(template)
                .modelObject(modelObject)
                .inlinePictures(inlinePictures)
                .build();
        queues[desiredPriorityLevel - 1].add(emailTemplateSchedulingData);
        log.info("Scheduled email {} at UTC time {} with priority {} with template", mimeEmail, scheduledDateTime, desiredPriorityLevel);
        if (isNull(timeOfNextScheduledMessage) || scheduledDateTime.toInstant().toEpochMilli() < timeOfNextScheduledMessage) {
            notify();
        }
    }

    public synchronized ServiceStatus status() {
        return serviceStatus;
    }

    private int normalizePriority(int priorityLevel) {
        //the priority level must be between 0 and numberOfPriorityLevels
        final int maxLevel = queues.length;
        if (priorityLevel > maxLevel) {
            log.warn("Scheduled email with priority level {}, while priority level {} was requested. Reason: max level exceeded",
                    maxLevel, priorityLevel);
        }
        return max(1, min(priorityLevel, maxLevel));
    }

    private synchronized Optional<EmailSchedulingData> dequeue() throws InterruptedException {
        EmailSchedulingData emailSchedulingData = null;
        timeOfNextScheduledMessage = null;
        while (consumer.enabled() && isNull(emailSchedulingData)) {
            //try to find a message in queue
            final long now = TimeUtils.now();
            for (final TreeSet<EmailSchedulingData> queue : queues) {
                if (!queue.isEmpty()) {
                    final long time = queue.first().getScheduledDateTime().toInstant().toEpochMilli();
                    if (time - now <= DELTA) {
                        //message found!
                        emailSchedulingData = queue.pollFirst();
                        break;
                    } else if (isNull(timeOfNextScheduledMessage) || time < timeOfNextScheduledMessage) {
                        timeOfNextScheduledMessage = time;
                    }

                }
            }
            if (isNull(emailSchedulingData)) {
                //no message was found, let's sleep, some message may arrive in the meanwhile
                if (isNull(timeOfNextScheduledMessage)) { //all the queues are empty
                    wait(); //the consumer starts waiting for a new email to be scheduled
                } else {
                    final long waitTime = timeOfNextScheduledMessage - TimeUtils.now() - DELTA;
                    if (waitTime > 0) {
                        wait(waitTime); //wait before sending the most imminent scheduled email
                    }
                }
            }
        }
        //here emailSchedulingData is the message to send
        return Optional.ofNullable(emailSchedulingData);
    }

    private void checkPriorityLevel(int priorityLevel) {
        checkArgument(priorityLevel > 0, "The priority level index cannot be negative");
    }

    @PreDestroy
    protected synchronized void cleanUp() throws Exception {
        consumer.close();
        serviceStatus = ServiceStatus.CLOSED;
        notify(); //the consumer, if waiting, is notified and can try to close
    }

    class Consumer extends Thread {

        private boolean canRun = true;

        public void run() {
            log.info("DefaultEmail scheduler consumer started");
            while (canRun) {
                try {
                    final Optional<EmailSchedulingData> emailSchedulingWrapperOptional = dequeue();
                    if (canRun && emailSchedulingWrapperOptional.isPresent()) {
                        final EmailSchedulingData emailSchedulingData = emailSchedulingWrapperOptional.get();
                        if (emailSchedulingData instanceof TemplateEmailSchedulingData) {
                            final TemplateEmailSchedulingData emailTemplateSchedulingData =
                                    (TemplateEmailSchedulingData) emailSchedulingData;
                            try {
                                emailService.send(emailTemplateSchedulingData.getEmail(),
                                        emailTemplateSchedulingData.getTemplate(),
                                        emailTemplateSchedulingData.getModelObject(),
                                        emailTemplateSchedulingData.getInlinePictures());
                            } catch (final CannotSendEmailException e) {
                                log.error("An error occurred while sending the email", e);
                            }
                        } else {
                            emailService.send(emailSchedulingData.getEmail());
                        }
                    } else {
                        log.info("DefaultEmail scheduler consumer stopped");
                    }
                } catch (final InterruptedException e) {
                    log.error("DefaultEmail scheduler consumer interrupted", e);
                }
            }
            log.info("DefaultEmail scheduler consumer stopped");
        }

        public synchronized boolean enabled() {
            return canRun;
        }

        public synchronized void close() {
            canRun = false;
        }

    }

}