//package it.ozimov.springboot.templating.mail.service;
//
//import it.ozimov.springboot.templating.mail.CoreTestApplication;
//import org.assertj.core.api.JUnitSoftAssertions;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.junit.rules.Timeout;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.Assert.*;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = CoreTestApplication.class, webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class EmbededRedisTest {
//
//    @Rule
//    public ExpectedException expectedException = ExpectedException.none();
//
//    @Rule
//    public Timeout globalTimeout = new Timeout(10, TimeUnit.SECONDS);
//
//    @Rule
//    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
//
//    @Test
//    public void startRedis() throws Exception {
//
//    }
//
//    @Test
//    public void stopRedis() throws Exception {
//
//    }
//
//}