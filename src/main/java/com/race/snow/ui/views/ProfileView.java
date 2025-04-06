package com.race.snow.ui.views;

import com.race.snow.model.User;
import com.race.snow.service.UserService;
import com.race.snow.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Mi Perfil | Agenda Gerencial")
@PermitAll
public class ProfileView extends VerticalLayout {

    private final UserService userService;
    private User currentUser;
    
    private final TextField name = new TextField("Nombre");
    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Nueva Contraseña");
    private final PasswordField confirmPassword = new PasswordField("Confirmar Contraseña");
    
    private final Binder<User> binder = new Binder<>(User.class);

    @Autowired
    public ProfileView(UserService userService) {
        this.userService = userService;
        
        addClassName("profile-view");
        setMaxWidth("600px");
        setAlignItems(Alignment.CENTER);
        
        loadCurrentUser();
        configureForm();
        
        Button saveButton = new Button("Guardar", e -> saveProfile());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        add(createFormLayout(), new HorizontalLayout(saveButton));
    }
    
    private void loadCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));
    }
    
    private void configureForm() {
        binder.forField(name)
            .asRequired("El nombre es obligatorio")
            .bind(User::getName, User::setName);
            
        binder.forField(email)
            .asRequired("El email es obligatorio")
            .withValidator(email -> email.contains("@"), "Ingrese un email válido")
            .bind(User::getEmail, User::setEmail);
        
        // No binding for password fields as they are special cases
        
        if (currentUser != null) {
            binder.readBean(currentUser);
        }
    }
    
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        
        password.setHelperText("Dejar en blanco para mantener la contraseña actual");
        confirmPassword.setHelperText("Repita la nueva contraseña");
        
        formLayout.add(name, email, password, confirmPassword);
        formLayout.setMaxWidth("100%");
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        
        return formLayout;
    }
    
    private void saveProfile() {
        try {
            binder.writeBean(currentUser);
            
            // Handle password change logic
            if (!password.getValue().isEmpty()) {
                if (!password.getValue().equals(confirmPassword.getValue())) {
                    Notification.show("Las contraseñas no coinciden")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                
                // Set the new password
                currentUser.setPassword(password.getValue());
            }
            
            userService.save(currentUser);
            
            Notification.show("Perfil actualizado correctamente")
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
            // Clear password fields
            password.clear();
            confirmPassword.clear();
            
        } catch (ValidationException e) {
            Notification.show("Error al guardar: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
