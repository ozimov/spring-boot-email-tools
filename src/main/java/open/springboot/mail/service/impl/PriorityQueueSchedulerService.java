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

import open.springboot.mail.model.Email;
import open.springboot.mail.model.EmailSchedulingWrapper;
import open.springboot.mail.model.EmailTemplateSchedulingWrapper;
import open.springboot.mail.model.InlinePicture;
import open.springboot.mail.service.EmailService;
import open.springboot.mail.service.Exception.CannotSendEmailException;
import open.springboot.mail.service.SchedulerService;

import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Map;
import java.util.TreeSet;

public class PriorityQueueSchedulerService implements SchedulerService {

    private static final long DELTA = 1000; //millisecs elapsed form the call of the send method and the actual sending by SMTP server
    private static final int DEFAULT_N_OF_PRIORITY_LEVELS = 10;

    private static final PriorityQueueSchedulerService singleton;

    private Long timeOfNextScheduledMessage;

    static {
        Integer numberOfPriorirtyLevels;
        try {
            numberOfPriorirtyLevels = Integer.parseInt(System.getProperty("numberOfPriorirtyLevels")); //TODO: come inizializziamo questo, ma anche gli altri parametri?
        } catch (Exception e) {
            numberOfPriorirtyLevels = DEFAULT_N_OF_PRIORITY_LEVELS;
        }
        singleton = new PriorityQueueSchedulerService(numberOfPriorirtyLevels);
    }

    protected TreeSet<EmailSchedulingWrapper>[] queues;


    protected EmailService es; //TODO: qual è il modo migliore per inizializzare questa variabile? si passa col costruttore o si fa l'injection?

    private PriorityQueueSchedulerService(int numberOfPriorirtyLevels) {
        queues = new TreeSet[numberOfPriorirtyLevels];
        for (int i = 0; i < numberOfPriorirtyLevels; i++) {
            queues[i] = new TreeSet<>();
        }
        new Consumer().start();
    }

    public static PriorityQueueSchedulerService getInstance() {
        return singleton;
    }

    @Override
    public synchronized void schedule(Email mimeEmail, Date scheduledDate, int priorityLevel) {
        priorityLevel = Math.max(0, Math.min(priorityLevel, queues.length - 1)); //the priority level must be between 0 and numberOfPriorirtyLevels-1
        EmailSchedulingWrapper esw = new EmailSchedulingWrapper(mimeEmail, scheduledDate, priorityLevel);
        queues[priorityLevel].add(esw);
        if (timeOfNextScheduledMessage==null || scheduledDate.getTime()<timeOfNextScheduledMessage) {
            notify();
        }
    }

    @Override
    public synchronized void schedule(Email mimeEmail, String template, Map<String, Object> modelObject, Date scheduledDate, int priorityLevel, InlinePicture... inlinePictures) throws CannotSendEmailException {
        priorityLevel = Math.max(0, Math.min(priorityLevel, queues.length - 1));
        EmailTemplateSchedulingWrapper esw = new EmailTemplateSchedulingWrapper(mimeEmail, scheduledDate, priorityLevel, template, modelObject, inlinePictures);
        queues[priorityLevel].add(esw);
        if (timeOfNextScheduledMessage==null || scheduledDate.getTime()<timeOfNextScheduledMessage) {
            notify();
        }
    }

    private synchronized EmailSchedulingWrapper dequeue() throws InterruptedException {
        EmailSchedulingWrapper esw = null;
        timeOfNextScheduledMessage = null;
        while (esw == null) {
            //try to find a message in queue
            long now = System.currentTimeMillis();
            for (TreeSet<EmailSchedulingWrapper> q : queues) {
                if (!q.isEmpty()) {
                    long time = q.first().getScheduledDate().getTime();
                    if (time - now <= DELTA) {
                        //message found!
                        esw = q.pollFirst();
                        break;
                    } else {
                        if (timeOfNextScheduledMessage == null || time < timeOfNextScheduledMessage) {
                            timeOfNextScheduledMessage = time;
                        }
                    }
                }
            }
            if (esw == null) {
                //no message was found, let's sleep, some message may arrive in the meanwhile
                if (timeOfNextScheduledMessage==null) { //all the queues are empty
                    wait(); //wait for a new email to be scheduled
                } else {
                    long waitTime=timeOfNextScheduledMessage-System.currentTimeMillis()-DELTA;
                    if (waitTime>0) {
                        wait(waitTime); //wait before sending the most imminent scheduled email
                    }
                }
            }
        }
        //here esw is the message to send
        return esw;
    }

    class Consumer extends Thread {

        public void run() {
            while (true) {
                try {
                    EmailSchedulingWrapper esw = singleton.dequeue();
                    if (esw instanceof EmailTemplateSchedulingWrapper) {
                        EmailTemplateSchedulingWrapper etsw = (EmailTemplateSchedulingWrapper) esw;
                        try {
                            es.send(etsw.getEmail(), etsw.getTemplate(), etsw.getModelObject(), etsw.getInlinePictures());
                        } catch (CannotSendEmailException e) {
                            //TODO: log the failure
                            e.printStackTrace();
                        }
                    } else {
                        es.send(esw.getEmail());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
