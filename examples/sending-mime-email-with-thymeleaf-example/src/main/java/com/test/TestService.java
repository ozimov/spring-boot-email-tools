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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

@Service
public class TestService {

    @Autowired
    private EmailService emailService;

    public void sendMimeEmailWithThymeleaf() throws IOException, CannotSendEmailException, URISyntaxException {
        InlinePicture inlinePicture = createGalaxyInlinePicture();

        final Email email = DefaultEmail.builder()
                .from(new InternetAddress("hari.seldon@gmail.com",
                        "Hari Seldon"))
                .to(newArrayList(
                        new InternetAddress("the-real-cleon@trantor.gov",
                                "Cleon I")))
                .subject("You shall die! It's not me, it's Psychohistory")
                .body("")//this will be overridden by the template, anyway
                .attachment(getPdfWithAccentedCharsAttachment("Conditions_Générales_Service"))
                .encoding("UTF-8").build();

        String template = "subfolder/emailTemplate";

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

    private EmailAttachment getPdfWithAccentedCharsAttachment(String filename) throws URISyntaxException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File pdfFile = new File(classLoader.getResource("attachments" + File.separator + "Questo documento è un test.pdf").toURI());

        final DefaultEmailAttachment attachment = DefaultEmailAttachment.builder()
                .attachmentName(filename + ".pdf")
                .attachmentData(Files.readAllBytes(pdfFile.toPath()))
                .mediaType(MediaType.APPLICATION_PDF).build();
        return attachment;
    }

}
