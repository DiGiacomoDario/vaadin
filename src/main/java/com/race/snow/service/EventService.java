package com.race.snow.service;

import com.race.snow.model.Event;
import com.race.snow.model.User;
import com.race.snow.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EmailService emailService;

    @Autowired
    public EventService(EventRepository eventRepository, EmailService emailService) {
        this.eventRepository = eventRepository;
        this.emailService = emailService;
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public List<Event> findByUser(User user) {
        return eventRepository.findByUser(user);
    }

    public List<Event> findByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByUserAndStartBetween(user, start, end);
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public Event save(Event event) {
        boolean isNew = event.getId() == null;
        Event savedEvent = eventRepository.save(event);
        
        // Send notification email
        if (event.getUser() != null && event.getUser().getEmail() != null) {
            if (isNew) {
                emailService.sendEventCreatedNotification(event);
            } else {
                emailService.sendEventUpdatedNotification(event);
            }
        }
        
        return savedEvent;
    }

    public void delete(Event event) {
        eventRepository.delete(event);
    }

    public void deleteById(Long id) {
        eventRepository.deleteById(id);
    }
    
    public void cleanupOldEvents() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Event> oldEvents = eventRepository.findByStartBefore(sixMonthsAgo);
        eventRepository.deleteAll(oldEvents);
    }
}
