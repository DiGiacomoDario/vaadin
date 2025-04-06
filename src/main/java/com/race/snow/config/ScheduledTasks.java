package com.race.snow.config;

import com.race.snow.model.Event;
import com.race.snow.repository.EventRepository;
import com.race.snow.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    
    private final EventRepository eventRepository;
    
    @Autowired
    public ScheduledTasks(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }
    
    // Ejecutar cada día a las 00:01
    @Scheduled(cron = "0 1 0 * * ?")
    public void cleanupOldEvents() {
        logger.info("Iniciando limpieza de eventos antiguos...");
        
        // Eliminar eventos que terminaron hace más de 1 año
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        List<Event> oldEvents = eventRepository.findAll().stream()
                .filter(event -> event.getEnd().isBefore(oneYearAgo))
                .toList();
        
        if (!oldEvents.isEmpty()) {
            eventRepository.deleteAll(oldEvents);
            logger.info("Eliminados {} eventos antiguos", oldEvents.size());
        } else {
            logger.info("No se encontraron eventos antiguos para eliminar");
        }
    }
}
