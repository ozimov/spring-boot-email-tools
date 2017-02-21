package com.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class StartStopStartApplication {

    private final static Logger logger = LoggerFactory.getLogger(StartStopStartApplication.class);

    public static void main(String... args) throws InterruptedException {
        logger.info("STARTED TEST FOR PERSISTENCE WITH REDIS");

        SchedulingAndPersistenceApplication.createMainSpringApp(true);

        TimeUnit.SECONDS.sleep(15);
        logger.info("EMAIL SCHEDULING AND PERSISTENCE APPLICATION SHUTTED DOWN");
        logger.info("EMAIL LOADING FROM PERSISTENCE LAYER AND DELIVERY APPLICATION STARTED");

        SchedulingAndPersistenceApplication.createMainSpringApp(false);
    }

}
