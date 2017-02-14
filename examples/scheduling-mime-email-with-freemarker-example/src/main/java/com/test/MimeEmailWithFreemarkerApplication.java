package com.test;

import it.ozimov.springboot.templating.mail.service.exception.CannotSendEmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ComponentScan(basePackages = {"com.test", "it.ozimov.springboot.templating.mail"})
public class MimeEmailWithFreemarkerApplication {

    @Autowired
    private TestService testService;

    public static void main(String[] args) {
        SpringApplication.run(MimeEmailWithFreemarkerApplication.class, args);
    }

    @PostConstruct
    public void sendEmail() throws UnsupportedEncodingException, InterruptedException, CannotSendEmailException {
        testService.sendMimeEmailWithFreemarker();

        close();
    }

    private void close() {
        TimerTask shutdownTask = new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        };
        Timer shutdownTimer = new Timer();
        shutdownTimer.schedule(shutdownTask, TimeUnit.SECONDS.toMillis(3));
    }

}