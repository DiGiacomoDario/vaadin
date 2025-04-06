package com.race.snow.ui.views;

import com.race.snow.model.User;
import com.race.snow.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class MainLayout extends AppLayout {
    
    private final UserService userService;
    private Tabs tabs;
    private User currentUser;
    
    public MainLayout(UserService userService) {
        this.userService = userService;
        loadCurrentUser();
        createHeader();
        createDrawer();
    }
    
    private void loadCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            userOpt.ifPresent(user -> this.currentUser = user);
        }
    }
    
    private void createHeader() {
        H1 logo = new H1("Calendario Gerentes");
        logo.addClassNames("text-l", "m-m");
        
        HorizontalLayout header = new HorizontalLayout(
            new DrawerToggle(),
            logo
        );
        
        // Add logout button
        Button logout = new Button("Cerrar sesión", new Icon(VaadinIcon.SIGN_OUT));
        logout.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
        });
        
        Span userInfo = new Span();
        if (currentUser != null) {
            userInfo.setText(currentUser.getName());
        }
        
        header.add(userInfo, logout);
        
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");
        
        addToNavbar(header);
    }
    
    private void createDrawer() {
        tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        
        tabs.add(createTab(VaadinIcon.CALENDAR, "Calendario", CalendarView.class));
        tabs.add(createTab(VaadinIcon.USER, "Mi Perfil", ProfileView.class));
        
        if (currentUser != null && currentUser.hasRole("ROLE_ADMIN")) {
            tabs.add(createTab(VaadinIcon.USERS, "Gestionar Gerentes", UsersView.class));
        }
        
        addToDrawer(tabs);
    }
    
    private Tab createTab(VaadinIcon viewIcon, String viewName, Class<? extends Component> viewClass) {
        Icon icon = viewIcon.create();
        icon.getStyle().set("box-sizing", "border-box")
            .set("margin-inline-end", "var(--lumo-space-m)")
            .set("margin-inline-start", "var(--lumo-space-xs)")
            .set("padding", "var(--lumo-space-xs)");
        
        RouterLink link = new RouterLink();
        link.add(icon, new Span(viewName));
        link.setRoute(viewClass);
        link.setTabIndex(-1);
        
        return new Tab(link);
    }
}
