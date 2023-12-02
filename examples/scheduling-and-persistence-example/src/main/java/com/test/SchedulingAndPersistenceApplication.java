package com.test;

import it.ozimov.springboot.mail.configuration.EnableEmailTools;
import it.ozimov.springboot.mail.service.exception.CannotSendEmailException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import jakarta.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableAsync
@EnableEmailTools
public class SchedulingAndPersistenceApplication implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static boolean scheduleEmails = true;

    @Autowired
    private TestService testService;

    public static void createMainSpringApp(final boolean schedule) {
        scheduleEmails = schedule;
        SpringApplication.run(SchedulingAndPersistenceApplication.class);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void sendEmail() throws UnsupportedEncodingException, InterruptedException, CannotSendEmailException {
        if (scheduleEmails) testService.scheduleTwoEmails();

        close();
    }

    @Async
    private void close() {
        final TimerTask shutdownTask = new TimerTask() {
            @Override
            public void run() {
                ((ConfigurableApplicationContext) applicationContext).close();
            }
        };
        Timer shutdownTimer = new Timer();
        shutdownTimer.schedule(shutdownTask, TimeUnit.SECONDS.toMillis(10));
    }

}