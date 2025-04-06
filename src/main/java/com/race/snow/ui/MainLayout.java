package com.race.snow.ui;

import com.race.snow.ui.views.CalendarView;
import com.race.snow.ui.views.ProfileView;
import com.race.snow.ui.views.UserListView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class MainLayout extends AppLayout {
    
    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Agenda Gerencial");
        logo.addClassNames("text-l", "m-m");

        Button logoutButton = new Button("Cerrar sesión", e -> {
            getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
        });

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logoutButton);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        // Add main navigation links
        RouterLink calendarLink = new RouterLink("Calendario", CalendarView.class);
        RouterLink profileLink = new RouterLink("Mi Perfil", ProfileView.class);
        
        VerticalLayout drawerContent = new VerticalLayout(calendarLink, profileLink);
        
        // Add admin links if user has admin role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
                
        if (isAdmin) {
            RouterLink usersLink = new RouterLink("Gestión de Usuarios", UserListView.class);
            drawerContent.add(usersLink);
        }
        
        drawerContent.setSizeFull();
        addToDrawer(drawerContent);
    }
}
