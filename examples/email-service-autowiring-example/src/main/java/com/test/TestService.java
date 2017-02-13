package com.test;

import it.ozimov.springboot.templating.mail.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Autowired
    private EmailService emailService;

}
