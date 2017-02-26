package com.test;

import it.ozimov.springboot.mail.configuration.EnableEmailTools;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEmailTools
public class AutowireApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutowireApplication.class, args);
    }

}