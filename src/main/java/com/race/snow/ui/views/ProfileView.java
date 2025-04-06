package com.race.snow.ui.views;

import com.race.snow.model.User;
import com.race.snow.service.UserService;
import com.race.snow.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.security.PermitAll;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Mi Perfil | Calendario Gerentes")
@PermitAll
public class ProfileView extends VerticalLayout {
    
    private final UserService userService;
    private final User currentUser;
    
    private final TextField nameField = new TextField("Nombre");
    private final EmailField emailField = new EmailField("Email");
    private final TextField usernameField = new TextField("Nombre de usuario");
    
    private final PasswordField currentPasswordField = new PasswordField("Contraseña actual");
    private final PasswordField newPasswordField = new PasswordField("Nueva contraseña");
    private final PasswordField confirmPasswordField = new PasswordField("Confirmar contraseña");
    
    private final Binder<User> binder = new Binder<>(User.class);
    
    public ProfileView(UserService userService) {
        this.userService = userService;
        
        // Obtener usuario actual
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        currentUser = userService.findByUsername(username).orElseThrow();
        
        addClassName("profile-view");
        setSpacing(true);
        setPadding(true);
        
        // Formulario de información general
        H2 generalInfoTitle = new H2("Información Personal");
        FormLayout generalInfoForm = new FormLayout();
        
        usernameField.setReadOnly(true);
        
        binder.forField(nameField).asRequired("El nombre es obligatorio").bind(User::getName, User::setName);
        binder.forField(emailField).asRequired("El email es obligatorio").bind(User::getEmail, User::setEmail);
        binder.forField(usernameField).bind(User::getUsername, User::setUsername);
        
        Button saveGeneralInfoButton = new Button("Guardar cambios");
        saveGeneralInfoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveGeneralInfoButton.addClickListener(event -> saveGeneralInfo());
        
        generalInfoForm.add(nameField, emailField, usernameField);
        
        // Formulario de cambio de contraseña
        H2 passwordTitle = new H2("Cambiar Contraseña");
        FormLayout passwordForm = new FormLayout();
        
        Button changePasswordButton = new Button("Cambiar contraseña");
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changePasswordButton.addClickListener(event -> changePassword());
        
        passwordForm.add(currentPasswordField, newPasswordField, confirmPasswordField);
        
        // Cargar datos actuales
        binder.readBean(currentUser);
        
        add(
            generalInfoTitle,
            generalInfoForm,
            saveGeneralInfoButton,
            passwordTitle,
            passwordForm,
            changePasswordButton
        );
    }
    
    private void saveGeneralInfo() {
        try {
            binder.writeBean(currentUser);
            userService.save(currentUser);
            showSuccess("Datos actualizados correctamente");
        } catch (ValidationException e) {
            showError("Por favor, corrija los errores en el formulario");
        }
    }
    
    private void changePassword() {
        if (currentPasswordField.isEmpty()) {
            showError("Debe ingresar su contraseña actual");
            return;
        }
        
        if (newPasswordField.isEmpty()) {
            showError("Debe ingresar una nueva contraseña");
            return;
        }
        
        if (!newPasswordField.getValue().equals(confirmPasswordField.getValue())) {
            showError("Las contraseñas no coinciden");
            return;
        }
        
        boolean success = userService.changePassword(
            currentUser, 
            currentPasswordField.getValue(), 
            newPasswordField.getValue()
        );
        
        if (success) {
            showSuccess("Contraseña actualizada correctamente");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showError("La contraseña actual es incorrecta");
        }
    }
    
    private void showSuccess(String message) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
    
    private void showError(String message) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
