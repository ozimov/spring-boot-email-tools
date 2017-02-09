package com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.test", "it.ozimov.springboot.templating.mail"})
public class AutowireApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutowireApplication.class, args);
    }

}