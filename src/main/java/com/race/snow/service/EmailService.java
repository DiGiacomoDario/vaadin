package com.race.snow.service;

import com.race.snow.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEventCreationNotification(Event event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.getUser().getEmail());
        message.setSubject("Nuevo evento creado: " + event.getTitle());
        message.setText(
            String.format(
                "Hola %s,\n\nSe ha creado un nuevo evento:\n\nTítulo: %s\nDescripción: %s\nFecha inicio: %s\nFecha fin: %s\n\nSaludos,\nSistema de Calendario",
                event.getUser().getName(),
                event.getTitle(),
                event.getDescription(),
                event.getStart().format(DATE_FORMATTER),
                event.getEnd().format(DATE_FORMATTER)
            )
        );
        mailSender.send(message);
    }

    @Async
    public void sendEventUpdateNotification(Event event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.getUser().getEmail());
        message.setSubject("Evento actualizado: " + event.getTitle());
        message.setText(
            String.format(
                "Hola %s,\n\nSe ha actualizado un evento:\n\nTítulo: %s\nDescripción: %s\nFecha inicio: %s\nFecha fin: %s\n\nSaludos,\nSistema de Calendario",
                event.getUser().getName(),
                event.getTitle(),
                event.getDescription(),
                event.getStart().format(DATE_FORMATTER),
                event.getEnd().format(DATE_FORMATTER)
            )
        );
        mailSender.send(message);
    }
}
