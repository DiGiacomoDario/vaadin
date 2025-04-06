package com.race.snow.ui.views;

import com.race.snow.model.User;
import com.race.snow.service.UserService;
import com.race.snow.ui.MainLayout;
import com.race.snow.ui.components.UserForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "usuarios", layout = MainLayout.class)
@PageTitle("Gestión de Usuarios | Agenda Gerencial")
@RolesAllowed("ADMIN")
public class UserListView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class);
    private final TextField filterField = new TextField();
    private ListDataProvider<User> dataProvider;

    @Autowired
    public UserListView(UserService userService) {
        this.userService = userService;
        
        addClassName("user-list-view");
        setSizeFull();
        
        configureGrid();
        configureFilter();
        
        Button addUserButton = new Button("Nuevo Usuario", e -> openUserDialog(new User()));
        
        HorizontalLayout toolbar = new HorizontalLayout(filterField, addUserButton);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, filterField);
        
        add(toolbar, grid);
        
        updateList();
    }
    
    private void configureGrid() {
        grid.addClassNames("user-grid");
        grid.setSizeFull();
        
        grid.setColumns(); // Remove automatically generated columns
        grid.addColumn(User::getUsername).setHeader("Usuario").setSortable(true);
        grid.addColumn(User::getName).setHeader("Nombre").setSortable(true);
        grid.addColumn(User::getEmail).setHeader("Email").setSortable(true);
        grid.addColumn(user -> String.join(", ", user.getRoles())).setHeader("Roles");
        
        grid.getColumns().forEach(column -> column.setAutoWidth(true));
        
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                openUserDialog(event.getValue());
            }
        });
    }
    
    private void configureFilter() {
        filterField.setPlaceholder("Filtrar por nombre...");
        filterField.setClearButtonVisible(true);
        filterField.setValueChangeMode(ValueChangeMode.LAZY);
        filterField.addValueChangeListener(e -> {
            if (dataProvider != null) {
                dataProvider.setFilter(user -> 
                    filterField.isEmpty() || 
                    user.getName().toLowerCase().contains(filterField.getValue().toLowerCase()) ||
                    user.getUsername().toLowerCase().contains(filterField.getValue().toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(filterField.getValue().toLowerCase())
                );
            }
        });
    }
    
    private void updateList() {
        dataProvider = DataProvider.ofCollection(userService.findAll());
        grid.setDataProvider(dataProvider);
    }
    
    private void openUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        
        UserForm form = new UserForm(user);
        
        form.addSaveListener(e -> {
            userService.save(e.getUser());
            dialog.close();
            updateList();
            Notification.show("Usuario guardado correctamente");
        });
        
        form.addDeleteListener(e -> {
            if (e.getUser().getId() != null) {
                userService.deleteById(e.getUser().getId());
                Notification.show("Usuario eliminado correctamente");
            }
            dialog.close();
            updateList();
        });
        
        form.addCancelListener(e -> dialog.close());
        
        dialog.add(form);
        dialog.open();
    }
}
