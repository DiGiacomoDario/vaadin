package com.race.snow.ui.views;

import com.race.snow.model.User;
import com.race.snow.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Gerentes | Calendario Gerentes")
@RolesAllowed("ROLE_ADMIN")
public class UsersView extends VerticalLayout {
    
    private final UserService userService;
    
    private Grid<User> grid = new Grid<>(User.class);
    private TextField filterField = new TextField();
    
    public UsersView(UserService userService) {
        this.userService = userService;
        
        addClassName("users-view");
        setSizeFull();
        
        configureGrid();
        
        Button addButton = new Button("Añadir Gerente", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openUserForm(new User()));
        
        HorizontalLayout toolbar = getToolbar();
        
        add(new H2("Gerentes"), toolbar, grid);
        updateList();
    }
    
    private void configureGrid() {
        grid.addClassName("users-grid");
        grid.setSizeFull();
        
        grid.setColumns();
        grid.addColumn(User::getName).setHeader("Nombre").setSortable(true);
        grid.addColumn(User::getUsername).setHeader("Usuario").setSortable(true);
        grid.addColumn(User::getEmail).setHeader("Email").setSortable(true);
        
        grid.addComponentColumn(user -> {
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openUserForm(user));
            
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.addClickListener(e -> confirmDelete(user));
            
            HorizontalLayout buttons = new HorizontalLayout(editButton, deleteButton);
            buttons.setSpacing(true);
            return buttons;
        }).setHeader("Acciones");
        
        grid.getColumns().forEach(column -> column.setAutoWidth(true));
    }
    
    private HorizontalLayout getToolbar() {
        filterField.setPlaceholder("Filtrar por nombre...");
        filterField.setClearButtonVisible(true);
        filterField.setValueChangeMode(ValueChangeMode.LAZY);
        filterField.addValueChangeListener(e -> updateList(e.getValue()));
        
        Button addButton = new Button("Añadir Gerente", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openUserForm(new User()));
        
        HorizontalLayout toolbar = new HorizontalLayout(filterField, addButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        return toolbar;
    }
    
    private void updateList() {
        updateList("");
    }
    
    private void updateList(String filter) {
        List<User> users = userService.findAllManagers();
        
        if (filter != null && !filter.isEmpty()) {
            users = users.stream()
                    .filter(user -> user.getName().toLowerCase().contains(filter.toLowerCase()))
                    .toList();
        }
        
        grid.setItems(users);
    }
    
    private void openUserForm(User user) {
        boolean isNew = user.getId() == null;
        
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle(isNew ? "Nuevo Gerente" : "Editar Gerente");
        
        Binder<User> binder = new BeanValidationBinder<>(User.class);
        
        FormLayout formLayout = new FormLayout();
        
        TextField nameField = new TextField("Nombre");
        TextField usernameField = new TextField("Usuario");
        EmailField emailField = new EmailField("Email");
        PasswordField passwordField = new PasswordField("Contraseña");
        
        // Solo mostrar campo de contraseña para nuevos usuarios
        passwordField.setVisible(isNew);
        
        formLayout.add(nameField, usernameField, emailField, passwordField);
        
        binder.forField(nameField).asRequired("El nombre es obligatorio").bind(User::getName, User::setName);
        binder.forField(usernameField).asRequired("El usuario es obligatorio").bind(User::getUsername, User::setUsername);
        binder.forField(emailField).asRequired("El email es obligatorio").bind(User::getEmail, User::setEmail);
        
        if (isNew) {
            binder.forField(passwordField).asRequired("La contraseña es obligatoria").bind(User::getPassword, User::setPassword);
        }
        
        // Si es nuevo usuario, añadir rol de gerente por defecto
        if (isNew) {
            Set<String> roles = new HashSet<>();
            roles.add("ROLE_GERENTE");
            user.setRoles(roles);
        }
        
        // Botones
        Button saveButton = new Button("Guardar");
        Button cancelButton = new Button("Cancelar");
        
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        saveButton.addClickListener(e -> {
            if (binder.validate().isOk()) {
                try {
                    binder.writeBean(user);
                    userService.save(user);
                    dialog.close();
                    updateList();
                    showSuccess(isNew ? "Gerente creado" : "Gerente actualizado");
                } catch (Exception ex) {
                    showError("Error al guardar: " + ex.getMessage());
                }
            }
        });
        
        cancelButton.addClickListener(e -> dialog.close());
        
        dialog.add(formLayout);
        dialog.getFooter().add(cancelButton, saveButton);
        
        binder.readBean(user);
        
        dialog.open();
    }
    
    private void confirmDelete(User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Eliminar Gerente");
        dialog.setText("¿Está seguro que desea eliminar a " + user.getName() + "? Esta acción no se puede deshacer.");
        
        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");
        
        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> {
            userService.delete(user);
            updateList();
            showSuccess("Gerente eliminado");
        });
        
        dialog.open();
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
