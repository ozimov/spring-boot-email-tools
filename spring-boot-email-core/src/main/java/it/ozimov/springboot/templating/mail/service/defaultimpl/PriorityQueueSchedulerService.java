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
import it.ozimov.springboot.templating.mail.model.InlinePicture;
import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.templating.mail.model.defaultimpl.TemplateEmailSchedulingData;
import it.ozimov.springboot.templating.mail.service.EmailService;
import it.ozimov.springboot.templating.mail.service.PersistenceService;
import it.ozimov.springboot.templating.mail.service.SchedulerService;
import it.ozimov.springboot.templating.mail.service.ServiceStatus;
import it.ozimov.springboot.templating.mail.service.exception.CannotSendEmailException;
import it.ozimov.springboot.templating.mail.utils.TimeUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static it.ozimov.springboot.templating.mail.service.defaultimpl.ConditionalExpression.SCHEDULER_IS_ENABLED;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * The class provides a {@linkplain SchedulerService} implementation with priority queues and persistence.
 * <p>
 * Main logic for the thread wait-notify mechanism comes from {@see http://stackoverflow.com/a/8980307/1339429 }
 */
@Service("priorityQueueSchedulerService")
@ConditionalOnExpression(SCHEDULER_IS_ENABLED)
@Slf4j
public class PriorityQueueSchedulerService implements SchedulerService {

    /**
     * millisecs elapsed form the call of the send method and the actual sending by SMTP server
     */
    protected static final long CYCLE_LENGTH_IN_MILLIS = SECONDS.toMillis(1);

    private final int batchSize;
    private final int minInMemory;
    private final int maxInMemory;

    private ServiceStatus serviceStatus = ServiceStatus.CREATED;
    private Long timeOfNextScheduledMessage;

    private final TreeSet<EmailSchedulingData>[] queues;
    private final EmailSchedulingData[] lastLoadedFromPersistenceLayer;

    private final EmailService emailService;

    private final Consumer consumer;

    private final Optional<PersistenceService> persistenceServiceOptional;

    @Autowired
    public PriorityQueueSchedulerService(
            @NonNull final EmailService emailService,
            @NonNull final SchedulerProperties schedulerProperties,
            @NonNull final Optional<PersistenceService> persistenceServiceOptional) {

        this.emailService = emailService;
        this.persistenceServiceOptional = persistenceServiceOptional;

        batchSize = nonNull(schedulerProperties.getPersistence()) ?
                schedulerProperties.getPersistence().getDesiredBatchSize() : 0;
        minInMemory = nonNull(schedulerProperties.getPersistence()) ?
                schedulerProperties.getPersistence().getMinKeptInMemory() : 1;
        maxInMemory = nonNull(schedulerProperties.getPersistence()) ?
                schedulerProperties.getPersistence().getMaxKeptInMemory() : Integer.MAX_VALUE;

        final int numberOfPriorityLevels = schedulerProperties.getPriorityLevels();
        queues = new TreeSet[numberOfPriorityLevels];
        lastLoadedFromPersistenceLayer = new EmailSchedulingData[numberOfPriorityLevels];
        for (int i = 0; i < numberOfPriorityLevels; i++) {
            queues[i] = new TreeSet<>();
        }

        consumer = new Consumer();
        synchronized (consumer) {
            consumer.start();
            try {
                consumer.wait();
            } catch (final InterruptedException e) {
                log.error("Email scheduler consumer interrupted", e);
            }
        }
        loadBatchFromPersistenceLayer();
    }

    @Override
    public void schedule(@NonNull final Email mimeEmail, @NonNull final OffsetDateTime scheduledDateTime,
                         final int desiredPriorityLevel) {
        checkPriorityLevel(desiredPriorityLevel);

        final int assignedPriorityLevel = normalizePriority(desiredPriorityLevel);
        final EmailSchedulingData emailSchedulingData = buildEmailSchedulingData(mimeEmail, scheduledDateTime, desiredPriorityLevel, assignedPriorityLevel);
        schedule(emailSchedulingData);
        log.info("Scheduled email {} at UTC time {} with priority {}", mimeEmail, scheduledDateTime, desiredPriorityLevel);
        if (isNull(timeOfNextScheduledMessage) || scheduledDateTime.toInstant().toEpochMilli() < timeOfNextScheduledMessage) {
            synchronized (consumer) {
                consumer.notify(); //the consumer, if waiting, is notified and can try to send next scheduled message
            }
        }
    }

    @Override
    public void schedule(@NonNull final Email mimeEmail,
                         @NonNull final OffsetDateTime scheduledDateTime,
                         final int desiredPriorityLevel,
                         @NonNull final String template,
                         @NonNull final Map<String, Object> modelObject,
                         final InlinePicture... inlinePictures) throws CannotSendEmailException {
        checkPriorityLevel(desiredPriorityLevel);

        final int assignedPriorityLevel = normalizePriority(desiredPriorityLevel);
        final EmailSchedulingData emailTemplateSchedulingData = buildEmailSchedulingData(mimeEmail, scheduledDateTime, desiredPriorityLevel, template, modelObject, assignedPriorityLevel, inlinePictures);
        schedule(emailTemplateSchedulingData);
        log.info("Scheduled email {} at UTC time {} with priority {} with template", mimeEmail, scheduledDateTime, desiredPriorityLevel);
        if (isNull(timeOfNextScheduledMessage) || scheduledDateTime.toInstant().toEpochMilli() < timeOfNextScheduledMessage) {
            synchronized (consumer) {
                consumer.notify(); //the consumer, if waiting, is notified and can try to send next scheduled message
            }
        }
    }


    protected EmailSchedulingData buildEmailSchedulingData(@NonNull Email mimeEmail, @NonNull OffsetDateTime scheduledDateTime, int desiredPriorityLevel, int assignedPriorityLevel) {
        return DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(mimeEmail)
                .scheduledDateTime(scheduledDateTime)
                .assignedPriority(assignedPriorityLevel)
                .desiredPriority(desiredPriorityLevel)
                .build();
    }

    protected EmailSchedulingData buildEmailSchedulingData(@NonNull Email mimeEmail, @NonNull OffsetDateTime scheduledDateTime, int desiredPriorityLevel, @NonNull String template, @NonNull Map<String, Object> modelObject, int assignedPriorityLevel, InlinePicture[] inlinePictures) {
        return TemplateEmailSchedulingData.templateEmailSchedulingDataBuilder()
                .email(mimeEmail)
                .scheduledDateTime(scheduledDateTime)
                .assignedPriority(assignedPriorityLevel)
                .desiredPriority(desiredPriorityLevel)
                .template(template)
                .modelObject(modelObject)
                .inlinePictures(inlinePictures)
                .build();
    }

    protected void schedule(final EmailSchedulingData emailSchedulingData) {
        synchronized (this) {
            final int queueIndex = queueIndex(emailSchedulingData);
            final boolean canAddOneInMemory = canAddOneInMemory();
            final boolean isAfterLastLoadedFromPersistenceLayer = afterLastLoadedFromPersistenceLayer(emailSchedulingData);
            if (canAddOneInMemory && !isAfterLastLoadedFromPersistenceLayer) {
                queues[queueIndex].add(emailSchedulingData);
            } else if (!canAddOneInMemory && !isAfterLastLoadedFromPersistenceLayer &&
                    //We cannot exceed the total size, but if the newly scheduled email is before one of the
                    //last then we need to keep it and push out one of those in the queues.
                    isBeforeOneLastLoadedFromPersistenceLayer(emailSchedulingData)) {
                queues[queueIndex].add(emailSchedulingData);
                int queueIndexOfLatestOfAllLast = queueIndexOfLatestOfAllLast();
                if (queueIndexOfLatestOfAllLast != -1) {
                    TreeSet<EmailSchedulingData> queue = queues[queueIndexOfLatestOfAllLast];
                    if (!queue.isEmpty()) {
                        queue.remove(queue.last());
                        if (!queue.isEmpty()) {
                            lastLoadedFromPersistenceLayer[queueIndexOfLatestOfAllLast] = queue.last();
                        }
                    }
                }
            }
            addToPersistenceLayer(emailSchedulingData);
        }
    }

    protected void loadBatchFromPersistenceLayer() {
        synchronized (this) {
            persistenceServiceOptional.ifPresent(
                    persistenceService -> {
                        Collection<EmailSchedulingData> emailSchedulingDataList =
                                persistenceService.getNextBatch(batchSize);
                        if (!emailSchedulingDataList.isEmpty()) {
                            scheduleBatch(emailSchedulingDataList);
                        }
                    }
            );
        }
    }

    protected void addToPersistenceLayer(final EmailSchedulingData emailSchedulingData) {
        persistenceServiceOptional.ifPresent(
                persistenceService ->
                        persistenceService.add(emailSchedulingData)
        );
    }

    protected void deleteFromPersistenceLayer(final EmailSchedulingData emailSchedulingData) {
        synchronized (this) {
            persistenceServiceOptional.ifPresent(
                    persistenceService -> {
                        persistenceService.remove(emailSchedulingData.getId());

                        final int currentlyInMemory = currentlyInMemory();
                        if (currentlyInMemory < minInMemory) {
                            final int expectedFromPersistenceLayer = Math.min(batchSize, maxInMemory - currentlyInMemory);
                            final Collection<EmailSchedulingData> emailSchedulingDataList =
                                    persistenceService.getNextBatch(expectedFromPersistenceLayer);
                            if (!emailSchedulingDataList.isEmpty()) {
                                scheduleBatch(emailSchedulingDataList);
                            }
                        }
                    }
            );
        }
    }

    protected void scheduleBatch(final Collection<EmailSchedulingData> emailSchedulingDataCollection) {
        checkArgument(!emailSchedulingDataCollection.isEmpty(), "Collection of EmailSchedulingData should not be empty.");

        Set<EmailSchedulingData> sortedEmailSchedulingData = new TreeSet<>(EmailSchedulingData.DEFAULT_COMPARATOR);
        sortedEmailSchedulingData.addAll(emailSchedulingDataCollection);
        EmailSchedulingData lastEmailSchedulingData = null;
        for (final EmailSchedulingData emailSchedulingData : sortedEmailSchedulingData) {
            lastEmailSchedulingData = emailSchedulingData;
            queues[queueIndex(emailSchedulingData)].add(emailSchedulingData);
            log.debug("Scheduled email {} at UTC time {} with assigned priority {}.",
                    emailSchedulingData.getEmail(),
                    emailSchedulingData.getScheduledDateTime(),
                    emailSchedulingData.getAssignedPriority());
        }

        setLastLoadedFromPersistenceLayer();

        if (isNull(timeOfNextScheduledMessage) || lastEmailSchedulingData.getScheduledDateTime().toInstant().toEpochMilli() < timeOfNextScheduledMessage) {
            synchronized (consumer) {
                consumer.notify(); //the consumer, if waiting, is notified and can try to send next scheduled message
            }
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

    private Optional<EmailSchedulingData> dequeue() throws InterruptedException {
        EmailSchedulingData emailSchedulingData = null;
        timeOfNextScheduledMessage = null;
        boolean consumerEnabled;
        synchronized (consumer) {
            consumerEnabled = consumer.enabled();
        }
        while (consumerEnabled && isNull(emailSchedulingData)) {
            //try to find a message in queue
            final long now = TimeUtils.now();
            synchronized (this) {
                for (final TreeSet<EmailSchedulingData> queue : queues) {
                    if (!queue.isEmpty()) {
                        final long time = queue.first().getScheduledDateTime().toInstant().toEpochMilli();
                        if (time - now <= CYCLE_LENGTH_IN_MILLIS) {
                            //message found!
                            emailSchedulingData = queue.pollFirst();
                            break;
                        } else if (isNull(timeOfNextScheduledMessage) || time < timeOfNextScheduledMessage) {
                            timeOfNextScheduledMessage = time;
                        }
                    }
                }
            }
            if (isNull(emailSchedulingData)) {
                //no message was found, let's sleep, some message may arrive in the meanwhile
                synchronized (consumer) {
                    if (isNull(timeOfNextScheduledMessage)) { //all the queues are empty
                        consumer.wait(); //the consumer starts waiting for a new email to be scheduled
                    } else {
                        final long waitTime = timeOfNextScheduledMessage - TimeUtils.now() - CYCLE_LENGTH_IN_MILLIS;
                        if (waitTime > 0) {
                            consumer.wait(waitTime); //wait before sending the most imminent scheduled email
                        }
                    }
                }
            }
        }
        //here emailSchedulingData is the message to send
        return ofNullable(emailSchedulingData);
    }

    private void checkPriorityLevel(int priorityLevel) {
        checkArgument(priorityLevel > 0, "The priority level index cannot be negative");
    }

    private int queueIndex(final EmailSchedulingData emailSchedulingData) {
        return emailSchedulingData.getAssignedPriority() - 1;
    }

    private void setLastLoadedFromPersistenceLayer() {
        IntStream.range(0, queues.length)
                .forEach(
                        i ->
                                lastLoadedFromPersistenceLayer[i] = queues[i].isEmpty() ?
                                        null : queues[i].last()
                );
    }

    private boolean canAddOneInMemory() {
        return !persistenceServiceOptional.isPresent() || currentlyInMemory() < maxInMemory;
    }

    private int currentlyInMemory() {
        synchronized (this) {
            return IntStream.range(0, queues.length)
                    .map(
                            queueIndex -> queues[queueIndex].size()
                    )
                    .sum();
        }
    }

    private boolean isBeforeOneLastLoadedFromPersistenceLayer(final EmailSchedulingData emailSchedulingData) {
        boolean found = false;
        boolean allNull = true;
        for (EmailSchedulingData lastOfPriorityLevel : lastLoadedFromPersistenceLayer) {
            if (nonNull(lastOfPriorityLevel)) {
                allNull = false;
                if (emailSchedulingData.compareTo(lastOfPriorityLevel) < 0) {
                    found = true;
                    break;
                }
            }
        }
        return found || allNull;
    }

    private int queueIndexOfLatestOfAllLast() {
        final Optional<EmailSchedulingData> latest =
                Arrays.stream(lastLoadedFromPersistenceLayer).filter(Objects::nonNull).max(EmailSchedulingData::compareTo);

        return latest.isPresent() ? latest.get().getAssignedPriority() - 1 : -1;
    }

    private boolean afterLastLoadedFromPersistenceLayer(final EmailSchedulingData emailSchedulingData) {
        if (!persistenceServiceOptional.isPresent()) {
            return false;
        }
        final int queueIndex = queueIndex(emailSchedulingData);
        return !isNull(lastLoadedFromPersistenceLayer[queueIndex]) && emailSchedulingData.compareTo(lastLoadedFromPersistenceLayer[queueIndex]) > 0;
    }

    @PreDestroy
    protected synchronized void cleanUp() throws Exception {
        consumer.close();
        serviceStatus = ServiceStatus.CLOSED;
        synchronized (consumer) {
            notify(); //the consumer, if waiting, is notified and can try to close
        }
    }

    class Consumer extends Thread {

        private boolean canRun = true;

        public void run() {
            log.info("Email scheduler consumer started");
            synchronized (this) {
                notify();
                while (canRun) {
                    try {
                        final Optional<EmailSchedulingData> emailSchedulingWrapperOptional = dequeue();
                        if (canRun && emailSchedulingWrapperOptional.isPresent()) {
                            final EmailSchedulingData emailSchedulingData = emailSchedulingWrapperOptional.get();
                            if (emailSchedulingData instanceof TemplateEmailSchedulingData) {
                                final TemplateEmailSchedulingData emailTemplateSchedulingData = (TemplateEmailSchedulingData) emailSchedulingData;
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
                            deleteFromPersistenceLayer(emailSchedulingData);
                        } else {
                            log.info("Email scheduler consumer stopped");
                        }
                    } catch (final InterruptedException e) {
                        log.error("Email scheduler consumer interrupted", e);
                    }
                }
            }
            log.info("Email scheduler consumer stopped");
        }

        public synchronized boolean enabled() {
            return canRun;
        }

        public synchronized void close() {
            canRun = false;
        }

    }

}