package com.race.snow.config;

import com.race.snow.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class SchedulingConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);
    
    private final EventService eventService;
    
    @Autowired
    public SchedulingConfig(EventService eventService) {
        this.eventService = eventService;
    }
    
    @Scheduled(cron = "0 0 1 * * ?") // Run at 1:00 AM every day
    public void cleanupOldEvents() {
        logger.info("Running scheduled task for cleaning up old events");
        eventService.cleanupOldEvents();
    }
}
