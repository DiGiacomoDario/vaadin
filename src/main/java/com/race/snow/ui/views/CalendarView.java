package com.race.snow.ui.views;

import com.race.snow.model.Event;
import com.race.snow.model.User;
import com.race.snow.service.EventService;
import com.race.snow.service.UserService;
import com.race.snow.ui.MainLayout;
import com.race.snow.ui.components.EventForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.Timezone;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Calendario | Calendario Gerentes")
@PermitAll
public class CalendarView extends VerticalLayout {

    private final FullCalendar calendar;
    private final EventService eventService;
    private final UserService userService;
    private User currentUser;

    @Autowired
    public CalendarView(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
        this.calendar = FullCalendarBuilder.create().build();
        
        setupCurrentUser();
        setSizeFull();
        
        // Configure calendar
        calendar.setSizeFull();
        calendar.setTimezone(Timezone.getSystem());
        calendar.setEditable(true);
        calendar.setSelectable(true);
        
        // Set up event handlers
        setupEventHandlers();
        
        // Add new event button
        Button addEventButton = new Button("Nuevo Evento", e -> openEventDialog(new Event()));
        
        Div calendarContainer = new Div(calendar);
        calendarContainer.setSizeFull();
        
        add(addEventButton, calendarContainer);
        
        // Load events
        loadEvents();
    }
    
    private void setupCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            currentUser = userOptional.get();
        } else {
            Notification.show("Error: Usuario no encontrado")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void setupEventHandlers() {
        // Handle date click
        calendar.addDateClickListener(event -> {
            Event newEvent = new Event();
            newEvent.setStart(event.getDate().atStartOfDay());
            newEvent.setEnd(event.getDate().atStartOfDay().plusHours(1));
            newEvent.setUser(currentUser);
            openEventDialog(newEvent);
        });
        
        // Handle entry click
        calendar.addEntryClickListener(event -> {
            Long eventId = Long.valueOf(event.getEntry().getId());
            eventService.findById(eventId).ifPresent(this::openEventDialog);
        });
        
        // Handle entry resize/move
        calendar.addEntryResizeListener(event -> updateEntryOnChange(event.getEntry()));
        calendar.addEntryDropListener(event -> updateEntryOnChange(event.getEntry()));
    }
    
    private void updateEntryOnChange(Entry entry) {
        Long eventId = Long.valueOf(entry.getId());
        eventService.findById(eventId).ifPresent(event -> {
            event.setStart(entry.getStart());
            event.setEnd(entry.getEnd());
            eventService.save(event);
            Notification.show("Evento actualizado");
        });
    }
    
    private void openEventDialog(Event event) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        
        EventForm form = new EventForm(event, currentUser);
        
        form.addSaveListener(e -> {
            Event savedEvent = eventService.save(e.getEvent());
            dialog.close();
            refreshCalendar();
            Notification.show("Evento guardado correctamente");
        });
        
        form.addDeleteListener(e -> {
            if (e.getEvent().getId() != null) {
                eventService.deleteById(e.getEvent().getId());
                Notification.show("Evento eliminado correctamente");
            }
            dialog.close();
            refreshCalendar();
        });
        
        form.addCancelListener(e -> dialog.close());
        
        dialog.add(form);
        dialog.open();
    }
    
    private void loadEvents() {
        calendar.removeAllEntries();
        
        List<Event> events;
        if (currentUser != null) {
            events = eventService.findByUser(currentUser);
        } else {
            return;
        }
        
        for (Event event : events) {
            Entry entry = new Entry(event.getId().toString());
            entry.setTitle(event.getTitle());
            entry.setDescription(event.getDescription());
            entry.setStart(event.getStart());
            entry.setEnd(event.getEnd());
            if (event.getColor() != null) {
                entry.setColor(event.getColor());
            }
            
            calendar.addEntry(entry);
        }
    }
    
    private void refreshCalendar() {
        loadEvents();
    }
}
