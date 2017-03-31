package it.ozimov.springboot.mail.logging;

import it.ozimov.springboot.mail.model.Email;

import javax.mail.internet.MimeMessage;

public interface EmailRenderer {

    String render(Email email);

}
