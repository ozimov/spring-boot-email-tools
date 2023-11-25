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

import it.ozimov.springboot.mail.configuration.EmailSchedulerProperties;
import it.ozimov.springboot.mail.logging.EmailLogRenderer;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.EmailSchedulingData;
import it.ozimov.springboot.mail.model.InlinePicture;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.mail.model.defaultimpl.TemplateEmailSchedulingData;
import it.ozimov.springboot.mail.service.EmailService;
import it.ozimov.springboot.mail.service.PersistenceService;
import it.ozimov.springboot.mail.service.EmailSchedulerService;
import it.ozimov.springboot.mail.service.ServiceStatus;
import it.ozimov.springboot.mail.service.exception.CannotSendEmailException;
import it.ozimov.springboot.mail.utils.TimeUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkArgument;
import static it.ozimov.springboot.mail.service.defaultimpl.ConditionalExpression.SCHEDULER_IS_ENABLED;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

/**
 * The class provides a {@linkplain EmailSchedulerService} implementation with priority queues and persistence.
 * <p>
 * Main logic for the thread wait-notify mechanism comes from {@see http://stackoverflow.com/a/8980307/1339429 }
 */
@Service("priorityQueueEmailSchedulerService")
@ConditionalOnExpression(SCHEDULER_IS_ENABLED)
@Slf4j
public class PriorityQueueEmailSchedulerService implements EmailSchedulerService {

    /**
     * millisecs elapsed form the call of the send method and the actual sending by SMTP server
     */
    protected static final Duration CONSUMER_CYCLE_LENGTH = Duration.of(1, ChronoUnit.SECONDS);

    /**
     * millisecs elapsed form the call of the send method and the actual sending by SMTP server
     */
    protected static final Duration RESUMER_CYCLE_LENGTH = Duration.of(5, ChronoUnit.SECONDS);

    private final int batchSize;
    private final int minInMemory;
    private final int maxInMemory;

    private volatile ServiceStatus serviceStatus = ServiceStatus.RUNNING;

    private AtomicLong timeOfNextScheduledMessage;

    private final PriorityQueueManager priorityQueueManager;

    private final EmailService emailService;

    private final Consumer consumer;

    private final Resumer resumer;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private Optional<PersistenceService> persistenceServiceOptional;

    private EmailLogRenderer emailLogRenderer;

    private final Lock schedulerLock = new ReentrantLock();

    @Autowired
    public PriorityQueueEmailSchedulerService(
            final EmailService emailService,
            final EmailSchedulerProperties emailSchedulerProperties,
            final Optional<PersistenceService> persistenceServiceOptional,
            final EmailLogRenderer emailLogRenderer) throws InterruptedException {

        this.emailService = emailService;
        this.persistenceServiceOptional = persistenceServiceOptional;
        this.emailLogRenderer = emailLogRenderer.registerLogger(log);

        timeOfNextScheduledMessage = new AtomicLong();

        batchSize = nonNull(emailSchedulerProperties.getPersistence()) ?
                emailSchedulerProperties.getPersistence().getDesiredBatchSize() : 0;
        minInMemory = nonNull(emailSchedulerProperties.getPersistence()) ?
                emailSchedulerProperties.getPersistence().getMinKeptInMemory() : 1;
        maxInMemory = nonNull(emailSchedulerProperties.getPersistence()) ?
                emailSchedulerProperties.getPersistence().getMaxKeptInMemory() : Integer.MAX_VALUE;

        final int numberOfPriorityLevels = emailSchedulerProperties.getPriorityLevels();
        priorityQueueManager = new PriorityQueueManager(numberOfPriorityLevels, persistenceServiceOptional.isPresent(),
                maxInMemory, CONSUMER_CYCLE_LENGTH);

        //CREATING EMAIL CONSUMER
        consumer = new Consumer();
        startConsumer();

        //CREATING EMAIL RESUMER
        if (this.persistenceServiceOptional.isPresent()) {
            resumer = new Resumer();
            startResumer();
        } else {
            resumer = null;
        }
    }

    @Override
    @Async
    public void schedule(@NonNull final Email mimeEmail, final int desiredPriorityLevel) {
        scheduleEmail(mimeEmail, TimeUtils.offsetDateTimeNow(), desiredPriorityLevel);
    }

    @Override
    @Async
    public void schedule(@NonNull final Email mimeEmail, @NonNull final OffsetDateTime scheduledDateTime, final int desiredPriorityLevel) {
        scheduleEmail(mimeEmail, scheduledDateTime, desiredPriorityLevel);
    }

    @Override
    @Async
    public void schedule(@NonNull final Email mimeEmail, final int desiredPriorityLevel, @NonNull final String template,
                         @NonNull final Map<String, Object> modelObject, final InlinePicture... inlinePictures) throws CannotSendEmailException {
        scheduleTemplateEmail(mimeEmail, TimeUtils.offsetDateTimeNow(), desiredPriorityLevel, template, modelObject, inlinePictures);
    }

    @Override
    @Async
    public void schedule(@NonNull final Email mimeEmail, @NonNull final OffsetDateTime scheduledDateTime, final int desiredPriorityLevel,
                         @NonNull final String template, @NonNull final Map<String, Object> modelObject, final InlinePicture... inlinePictures) throws CannotSendEmailException {
        scheduleTemplateEmail(mimeEmail, scheduledDateTime, desiredPriorityLevel, template, modelObject, inlinePictures);
    }

    private void scheduleEmail(final Email mimeEmail, final OffsetDateTime scheduledDateTime, final int desiredPriorityLevel) {
        checkPriorityLevel(desiredPriorityLevel);

        final int assignedPriorityLevel = normalizePriority(desiredPriorityLevel);
        final EmailSchedulingData emailSchedulingData = buildEmailSchedulingData(mimeEmail, scheduledDateTime, desiredPriorityLevel, assignedPriorityLevel);
        schedule(emailSchedulingData);

        emailLogRenderer.info("Scheduled email {} at UTC time {} with priority {}", mimeEmail, scheduledDateTime, desiredPriorityLevel);
        notifyConsumerIfCouldFire(scheduledDateTime);
    }

    private void scheduleTemplateEmail(final Email mimeEmail, final OffsetDateTime scheduledDateTime, final int desiredPriorityLevel,
                         final String template, final Map<String, Object> modelObject, final InlinePicture... inlinePictures) throws CannotSendEmailException {
        checkPriorityLevel(desiredPriorityLevel);

        final int assignedPriorityLevel = normalizePriority(desiredPriorityLevel);
        final EmailSchedulingData emailTemplateSchedulingData = buildEmailSchedulingData(mimeEmail, scheduledDateTime, desiredPriorityLevel, template, modelObject, assignedPriorityLevel, inlinePictures);
        schedule(emailTemplateSchedulingData);

        emailLogRenderer.info("Scheduled email {} at UTC time {} with priority {} with template", mimeEmail, scheduledDateTime, desiredPriorityLevel);
        notifyConsumerIfCouldFire(scheduledDateTime);
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

    protected synchronized void schedule(final EmailSchedulingData emailSchedulingData) {
        enqueueFromScheduler(emailSchedulingData);
        addToPersistenceLayer(emailSchedulingData);
        completeEnqueue();
    }

    protected synchronized void startResumer() throws InterruptedException {
        startAndWaitForWaitingState(resumer);
    }

    protected synchronized void startConsumer() throws InterruptedException {
        startAndWaitForWaitingState(consumer);
    }

    private void startAndWaitForWaitingState(final Thread thread) throws InterruptedException {
        thread.start();
        while (thread.getState() == Thread.State.RUNNABLE) {
            TimeUnit.MILLISECONDS.sleep(50);
        }
    }

    private void notifyConsumerIfCouldFire(@NonNull OffsetDateTime scheduledDateTime) {
        final boolean canFire = isTimeOfNextSchedulerMessageNotSet() || scheduledDateTime.toInstant().toEpochMilli() < timeOfNextScheduledMessage.get();
        if (canFire && consumer.enabled()) {
            executor.submit(() -> {
                synchronized (consumer) {
                    consumer.notify(); //the consumer, if waiting, is notified and can try to send next scheduled message
                }
            });
        }
    }

    // Returns true if the email is enqueued
    private boolean enqueueFromScheduler(final EmailSchedulingData emailSchedulingData) {
        return enqueue(emailSchedulingData, false);
    }


    private boolean enqueueFromPersistenceLayer(final EmailSchedulingData emailSchedulingData) {
        return enqueue(emailSchedulingData, true);
    }

    // Returns true if the email is enqueued
    private boolean enqueue(final EmailSchedulingData emailSchedulingData, final boolean isFromPersistenceLayer) {
        if (serviceStatus == ServiceStatus.RUNNING) {
            return priorityQueueManager.enqueue(emailSchedulingData, isFromPersistenceLayer);
        }
        return false;
    }

    private void completeEnqueue() {
        if (serviceStatus == ServiceStatus.RUNNING) {
            priorityQueueManager.completeEnqueue();
        }
    }

    protected void addToPersistenceLayer(final EmailSchedulingData emailSchedulingData) {
        if (serviceStatus == ServiceStatus.RUNNING) {
            log.debug("Adding to persistence layer");
            persistenceServiceOptional.ifPresent(
                    persistenceService ->
                            persistenceService.add(emailSchedulingData)
            );
        }
    }

    protected void deleteFromPersistenceLayer(final EmailSchedulingData emailSchedulingData) {
        //This part is disabled for now, I'm not sure that this could not provide overhead to the persistence layer
        if (serviceStatus == ServiceStatus.RUNNING) {
            persistenceServiceOptional.ifPresent(
                    persistenceService -> {
                        persistenceService.remove(emailSchedulingData.getId());
                        priorityQueueManager.completeDequeue();
                    }
            );
        }
    }

    protected void loadNextBatch() {
        if (serviceStatus == ServiceStatus.RUNNING) {
            persistenceServiceOptional.ifPresent(
                    persistenceService -> {
                        final int currentlyInMemory = currentlyInMemory();
                        if (currentlyInMemory < minInMemory) {
                            //Currently REDIS loads all from the beginning so we need to count also those currentlyInMemory. This must be improved.
                            final int expectedFromPersistenceLayer = Math.min(currentlyInMemory + batchSize, maxInMemory);
                            final Collection<EmailSchedulingData> emailSchedulingDataList =
                                    persistenceService.getNextBatch(expectedFromPersistenceLayer);
                            if (!emailSchedulingDataList.isEmpty()) {
                                enqueueBatch(emailSchedulingDataList);
                            }
                        }
                    }
            );
        }
    }

    protected void enqueueBatch(final Collection<EmailSchedulingData> emailSchedulingDataCollection) {
        if (!emailSchedulingDataCollection.isEmpty()) {
            EmailSchedulingData lastEmailSchedulingData = emailSchedulingDataCollection.stream()
                    .filter(Objects::nonNull)
                    .max((comparing(EmailSchedulingData::getScheduledDateTime))).get();
            if (serviceStatus == ServiceStatus.RUNNING) {
                int countAdded = 0;
                for (final EmailSchedulingData emailSchedulingData : emailSchedulingDataCollection) {
                    synchronized (this) {
                        if (enqueueFromPersistenceLayer(emailSchedulingData)) {
                            countAdded++;
                        }
                        completeEnqueue();
                    }
                }
                log.debug("Enqueued batch of {} emails of {} loaded from persistence layer.",
                        countAdded, emailSchedulingDataCollection.size());
            }

            if (nonNull(lastEmailSchedulingData)) {
                notifyConsumerIfCouldFire(lastEmailSchedulingData.getScheduledDateTime());
            }
        }
    }

    protected int normalizePriority(int priorityLevel) {
        //the priority level must be between 0 and numberOfPriorityLevels
        final int maxLevel = priorityQueueManager.numberOfLevels();
        if (priorityLevel > maxLevel) {
            log.warn("Scheduled email with priority level {}, while priority level {} was requested. Reason: max level exceeded",
                    maxLevel, priorityLevel);
        }
        return max(1, min(priorityLevel, maxLevel));
    }

    private Optional<EmailSchedulingData> dequeue() throws InterruptedException {
        Optional<EmailSchedulingData> emailSchedulingDataOptional = Optional.empty();
        timeOfNextScheduledMessage.set(0);
        boolean consumerEnabled = consumer.enabled();
        while (consumerEnabled && !emailSchedulingDataOptional.isPresent()) {
            if (consumer.enabled()) {
                //try to find a message in queue
                if (priorityQueueManager.hasElements()) {
                    emailSchedulingDataOptional = priorityQueueManager.dequeueNext(CONSUMER_CYCLE_LENGTH);
                }
                if (!emailSchedulingDataOptional.isPresent()) {
                    //no message was found, let's sleep, some message may arrive in the meanwhile
                    timeOfNextScheduledMessage.set(priorityQueueManager.millisToNextEmail());
                    if (consumer.enabled()) {
                        if (isTimeOfNextSchedulerMessageNotSet()) { //all the queues are empty
                            consumer.waitForNotify(); //the consumer starts waiting for a new email to be scheduled or
                        } else {
                            final long waitTime = timeOfNextScheduledMessage.get() - TimeUtils.now() - CONSUMER_CYCLE_LENGTH.toMillis();
                            if (waitTime > 0) {
                                consumer.waitForMillis(waitTime); //wait before sending the most imminent scheduled email
                            }
                        }
                    }
                }
            } else {
                consumerEnabled = false;
            }
        }
        //here emailSchedulingData is the message to send
        return emailSchedulingDataOptional;
    }

    private boolean isTimeOfNextSchedulerMessageNotSet() {
        return timeOfNextScheduledMessage.get() == 0L;
    }

    private void checkPriorityLevel(int priorityLevel) {
        checkArgument(priorityLevel > 0, "The priority level index cannot be negative");
    }

    private int queueIndex(final EmailSchedulingData emailSchedulingData) {
        return emailSchedulingData.getAssignedPriority() - 1;
    }

    private boolean canAddOneInMemory() {
        return !persistenceServiceOptional.isPresent() || currentlyInMemory() < maxInMemory;
    }

    private int currentlyInMemory() {
        return priorityQueueManager.currentlyInQueue();
    }

    @PreDestroy
    protected void cleanUp() throws Exception {
        log.info("Closing EmailScheduler");
        try {
            executor.shutdownNow();

            schedulerLock.lock();
            try {
                this.serviceStatus = ServiceStatus.CLOSING;
            } finally {
                schedulerLock.unlock();
            }

            log.debug("EMAIL SCHEDULER -- Closing PriorityQueueManager");
            priorityQueueManager.close();
            if (nonNull(resumer)) {
                log.debug("EMAIL SCHEDULER -- Closing Resumer");
                resumer.close();
            }
            log.debug("EMAIL SCHEDULER -- Closing Consumer");
            consumer.close();

        } catch (Exception e) {
            log.warn("An issue occurred while stopping EmailScheduler, it should be due to a thread interruption.", e);
        } finally {
            this.serviceStatus = ServiceStatus.CLOSED;
        }
        log.info("Closed EmailScheduler");
    }


    private class Consumer extends Thread {

        public Consumer() {
            super(PriorityQueueEmailSchedulerService.class.getSimpleName() + " -- " + Consumer.class.getSimpleName());
        }

        public void run() {
            log.info("Email scheduler consumer started");
            while (enabled()) {
                try {
                    final Optional<EmailSchedulingData> emailSchedulingDataOptional = dequeue();
                    if (enabled() && emailSchedulingDataOptional.isPresent()) {
                        final EmailSchedulingData emailSchedulingData = emailSchedulingDataOptional.get();
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
                        if (enabled() && !persistenceServiceOptional.isPresent()) {
                            priorityQueueManager.completeDequeue();
                        }

                        if (enabled()) deleteFromPersistenceLayer(emailSchedulingData);
                    }
                } catch (final InterruptedException e) {
                    log.error("Email scheduler consumer interrupted", e);
                }
            }
            log.info("Email scheduler consumer stopped");

        }

        public boolean enabled() {
            return serviceStatus == ServiceStatus.RUNNING && !isInterrupted();
        }

        public synchronized void waitForNotify() throws InterruptedException {
            if (enabled()) {
                log.debug("Email scheduler consumer starts waiting");
                wait();
            }
        }

        public synchronized void waitForMillis(final long timeoutInMillis) throws InterruptedException {
            if (enabled()) {
                log.debug("Email scheduler consumer starts waiting for {} millis", timeoutInMillis);
                wait(timeoutInMillis);
            }
        }

        public void close() throws InterruptedException {
            try {
                if (!isInterrupted()) {
                    log.info("Interrupting email scheduler consumer");
                    interrupt();
                    synchronized (this) {
                        notify();
                    }
                    join();
                } else {
                    log.info("Email scheduler consumer already interrupted");
                }
            } catch (InterruptedException e) {
            }
        }

    }

    private class Resumer extends Thread {

        public Resumer() {
            super(PriorityQueueEmailSchedulerService.class.getSimpleName() + " -- " + Resumer.class.getSimpleName());
        }

        public void run() {
            if (persistenceServiceOptional.isPresent()) {
                log.info("Email scheduler resumer started");
                while (enabled()) {
                    try {
                        if (canAddOneInMemory()) {
                            if (enabled()) {
                                loadNextBatch();
                            }
                            if (enabled()) {
                                synchronized (this) {
                                    wait(RESUMER_CYCLE_LENGTH.toMillis());
                                }
                            }
                        }
                    } catch (final InterruptedException e) {
                        log.error("Email scheduler consumer interrupted", e);
                    }
                }
                log.info("Email scheduler resumer stopped");
            } else {
                log.warn("Email scheduler resumer won't start because there is no email PersistenceService.");
            }
        }

        public boolean enabled() {
            return serviceStatus == ServiceStatus.RUNNING && !isInterrupted();
        }

        public synchronized void close() throws InterruptedException {
            try {
                if (!isInterrupted()) {
                    log.info("Interrupting email scheduler resumer");
                    interrupt();
                    synchronized (this) {
                        notify();
                    }
                    join();
                } else {
                    log.info("Email scheduler resumer already interrupted");
                }
            } catch (InterruptedException e) {
            }
        }

    }

}