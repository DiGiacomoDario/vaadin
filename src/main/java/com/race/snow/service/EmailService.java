package com.race.snow.service;

import com.race.snow.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender emailSender;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    public void sendEventCreatedNotification(Event event) {
        String subject = "Nuevo evento: " + event.getTitle();
        String content = createEventEmailContent(event, true);
        sendEmail(event.getUser().getEmail(), subject, content);
    }

    @Async
    public void sendEventUpdatedNotification(Event event) {
        String subject = "Evento actualizado: " + event.getTitle();
        String content = createEventEmailContent(event, false);
        sendEmail(event.getUser().getEmail(), subject, content);
    }

    private String createEventEmailContent(Event event, boolean isNew) {
        String action = isNew ? "creado" : "actualizado";
        
        return String.format(
            "<html><body>" +
            "<h2>Evento %s</h2>" +
            "<p>Estimado/a %s,</p>" +
            "<p>Se ha %s un evento en tu calendario:</p>" +
            "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
            "<p><strong>Título:</strong> %s</p>" +
            "<p><strong>Inicio:</strong> %s</p>" +
            "<p><strong>Fin:</strong> %s</p>" +
            "<p><strong>Descripción:</strong> %s</p>" +
            "</div>" +
            "<p>Saludos,<br>Sistema de Calendario</p>" +
            "</body></html>",
            action,
            event.getUser().getName(),
            action,
            event.getTitle(),
            event.getStart().format(formatter),
            event.getEnd().format(formatter),
            event.getDescription() != null ? event.getDescription() : "Sin descripción"
        );
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            emailSender.send(message);
            logger.info("Email notification sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email notification", e);
        }
    }
}
