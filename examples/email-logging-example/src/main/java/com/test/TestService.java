package com.test;

import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;

import static com.google.common.collect.Lists.newArrayList;

@Service
public class TestService {

    @Autowired
    private EmailService emailService;

    public void sendPlainTextEmailSoThatYouCanSeePrettyLogs() throws UnsupportedEncodingException {
        final Email email = DefaultEmail.builder()
                .from(new InternetAddress("hari.seldon@gmail.com",
                        "Hari Seldon"))
                .to(newArrayList(
                        new InternetAddress("the-real-cleon@trantor.gov",
                                "Cleon I")))
                .subject("You shall die! It's not me, it's Psychohistory")
                .body("Hello Planet!")
                .encoding("UTF-8").build();

        emailService.send(email);
    }

}
