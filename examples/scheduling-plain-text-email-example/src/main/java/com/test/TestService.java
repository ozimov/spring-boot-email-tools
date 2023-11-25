package com.test;

import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.service.EmailSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;

import static com.google.common.collect.Lists.newArrayList;

@Service
public class TestService {

    @Autowired
    private EmailSchedulerService emailSchedulerService;

    public void scheduleSixEmails() throws UnsupportedEncodingException {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime whenFirstGroup = now.plusSeconds(5);
        OffsetDateTime whenSecondGroup = now.plusSeconds(10);
        OffsetDateTime whenThirdGroup = now.plusSeconds(15);

        schedulePlainTextEmail(whenFirstGroup, 2);
        schedulePlainTextEmail(whenFirstGroup, 1);
        schedulePlainTextEmail(whenSecondGroup, 1);
        schedulePlainTextEmail(whenSecondGroup, 2);
        schedulePlainTextEmail(whenThirdGroup, 2);
        schedulePlainTextEmail(whenThirdGroup, 1);
    }

    private void schedulePlainTextEmail(OffsetDateTime when, int priority) throws UnsupportedEncodingException {
        final Email email = DefaultEmail.builder()
                .from(new InternetAddress("hari.seldon@gmail.com",
                        "Hari Seldon"))
                .to(newArrayList(
                        new InternetAddress("the-real-cleon@trantor.gov",
                                "Cleon I")))
                .subject(String.format("Email scheduled with firetime '%s' and priority %d", when, priority))
                .body("Hello Planet!")
                .encoding("UTF-8").build();

        emailSchedulerService.schedule(email, when, priority);
    }

}
