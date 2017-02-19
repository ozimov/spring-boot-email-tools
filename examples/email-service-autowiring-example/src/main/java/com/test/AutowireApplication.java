package com.test;

import it.ozimov.springboot.templating.mail.configuration.EnableEmailTools;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableEmailTools
public class AutowireApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutowireApplication.class, args);
    }

}