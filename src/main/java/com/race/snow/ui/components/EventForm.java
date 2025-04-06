package com.race.snow.ui.components;

import com.race.snow.model.Event;
import com.race.snow.model.User;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import java.time.LocalDateTime;

public class EventForm extends VerticalLayout {
    
    private final Binder<Event> binder = new BeanValidationBinder<>(Event.class);
    private final Event event;
    
    private final TextField title = new TextField("Título");
    private final TextArea description = new TextArea("Descripción");
    private final DateTimePicker start = new DateTimePicker("Inicio");
    private final DateTimePicker end = new DateTimePicker("Fin");
    private final TextField color = new TextField("Color (hex)");
    
    private final Button save = new Button("Guardar");
    private final Button delete = new Button("Eliminar");
    private final Button cancel = new Button("Cancelar");

    public EventForm(Event event, User user) {
        this.event = event;
        // Set user if new event
        if (event.getId() == null) {
            event.setUser(user);
        }
        
        addClassName("event-form");
        
        binder.bindInstanceFields(this);
        
        // Validators
        binder.forField(title)
              .asRequired("El título es obligatorio")
              .bind(Event::getTitle, Event::setTitle);
              
        binder.forField(start)
              .asRequired("La fecha de inicio es obligatoria")
              .bind(Event::getStart, Event::setStart);
              
        binder.forField(end)
              .asRequired("La fecha de fin es obligatoria")
              .withValidator(endDate -> endDate.isAfter(start.getValue()), 
                            "La fecha de fin debe ser posterior a la fecha de inicio")
              .bind(Event::getEnd, Event::setEnd);
              
        // Set default times if new event
        if (event.getId() == null && event.getStart() == null) {
            LocalDateTime now = LocalDateTime.now();
            start.setValue(now);
            end.setValue(now.plusHours(1));
        }
        
        // Read bean
        binder.readBean(event);
        
        add(
            new H3("Evento"),
            createFormLayout(),
            createButtonsLayout()
        );
    }
    
    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        
        description.setHeight("100px");
        
        formLayout.add(
            title,
            description,
            start,
            end,
            color
        );
        
        return formLayout;
    }
    
    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);
        
        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, this.event)));
        cancel.addClickListener(event -> fireEvent(new CancelEvent(this)));
        
        // Only show delete button for existing events
        delete.setVisible(this.event.getId() != null);
        
        return new HorizontalLayout(save, delete, cancel);
    }
    
    private void validateAndSave() {
        try {
            binder.writeBean(event);
            fireEvent(new SaveEvent(this, event));
        } catch (Exception e) {
            // Validation errors already shown in UI
        }
    }
    
    // Event classes
    public static abstract class EventFormEvent extends ComponentEvent<EventForm> {
        private final Event event;
        
        protected EventFormEvent(EventForm source, Event event) {
            super(source, false);
            this.event = event;
        }
        
        public Event getEvent() {
            return event;
        }
    }
    
    public static class SaveEvent extends EventFormEvent {
        SaveEvent(EventForm source, Event event) {
            super(source, event);
        }
    }
    
    public static class DeleteEvent extends EventFormEvent {
        DeleteEvent(EventForm source, Event event) {
            super(source, event);
        }
    }
    
    public static class CancelEvent extends EventFormEvent {
        CancelEvent(EventForm source) {
            super(source, null);
        }
    }
    
    // Listener registration methods
    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }
    
    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }
    
    public Registration addCancelListener(ComponentEventListener<CancelEvent> listener) {
        return addListener(CancelEvent.class, listener);
    }
}
