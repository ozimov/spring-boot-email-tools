package com.test;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.EmailAttachment;
import it.ozimov.springboot.mail.model.ImageType;
import it.ozimov.springboot.mail.model.InlinePicture;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailAttachment;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultInlinePicture;
import it.ozimov.springboot.mail.service.EmailService;
import it.ozimov.springboot.mail.service.exception.CannotSendEmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

@Service
public class TestService {

    @Autowired
    private EmailService emailService;

    public void sendMimeEmailWithMustache() throws UnsupportedEncodingException, CannotSendEmailException, URISyntaxException {
        InlinePicture inlinePicture = createGalaxyInlinePicture();

        final Email email = DefaultEmail.builder()
                .from(new InternetAddress("hari.seldon@the-foundation.gal",
                        "Hari Seldon"))
                .to(newArrayList(
                        new InternetAddress("the-real-cleon@trantor.gov",
                                "Cleon I")))
                .subject("You shall die! It's not me, it's Psychohistory")
                .body("")//this will be overridden by the template, anyway
                .attachment(getCsvForecastAttachment("forecast"))
                .encoding("UTF-8").build();

        String template = "emailTemplate.html";

        Map<String, Object> modelObject = ImmutableMap.of(
                "title", "Emperor",
                "name", "Cleon I"
        );

        emailService.send(email, template, modelObject, inlinePicture);
    }

    private InlinePicture createGalaxyInlinePicture() throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File pictureFile = new File(classLoader.getResource("images" + File.separator + "galaxy.jpeg").toURI());
        Preconditions.checkState(pictureFile.exists(), "There is not picture %s", pictureFile.getName());

        return DefaultInlinePicture.builder()
                .file(pictureFile)
                .imageType(ImageType.JPG)
                .templateName("galaxy.jpeg").build();
    }

    private EmailAttachment getCsvForecastAttachment(String filename) {
        final String testData = "years from now,death probability\n1,0.9\n2,0.95\n3,1.0";
        final DefaultEmailAttachment attachment = DefaultEmailAttachment.builder()
                .attachmentName(filename + ".csv")
                .attachmentData(testData.getBytes(Charset.forName("UTF-8")))
                .mediaType(MediaType.TEXT_PLAIN).build();
        return attachment;
    }

}
