package com.analyzer.sysanalyzer.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

@Service
public class MailService {
    private Session session;

    @Value("${state.alert.mail.username}")
    private String username;
    @Value("${state.alert.mail.password}")
    private String password;
    @Value("${state.alert.mail.from}")
    private String userFrom;
    @Value("${state.alert.mail.to}")
    private String userTo;

    @PostConstruct
    public void init() {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.ssl.checkserveridentity", true);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendMessage(String head, String body) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(userFrom));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(userTo));
        message.setSubject(head);
        message.setText(body);
        Transport.send(message);
    }
}
