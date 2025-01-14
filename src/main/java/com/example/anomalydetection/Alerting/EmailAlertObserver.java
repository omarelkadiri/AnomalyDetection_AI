package com.example.anomalydetection.Alerting;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailAlertObserver implements AlertObserver {
    private static final String EMAIL_TO = "to@gmail.com";
    private static final String EMAIL_FROM = "from@gmail.com";
    private static final String EMAIL_PASSWORD = "pass";

    @Override
    public void update(Alert alert) {
        if (alert.getSeverity() == Alert.Severity.HIGH ||
                alert.getSeverity() == Alert.Severity.CRITICAL) {
            sendEmailAlert(alert);
        }
    }

    private void sendEmailAlert(Alert alert) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_TO));

            String subject = String.format("Alerte de Sécurité [%s] - Anomalie Détectée",
                    alert.getSeverity());
            message.setSubject(subject);

            message.setText(alert.getDetails());

            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}