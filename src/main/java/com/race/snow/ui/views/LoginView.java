package com.race.snow.ui.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | Calendario Gerentes")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    
    private final LoginForm loginForm = new LoginForm();
    
    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        loginForm.setAction("login");
        
        Image logo = new Image("images/logo.png", "Logo");
        logo.setWidth("200px");
        
        add(
            logo,
            new H1("Calendario para Gerentes"),
            loginForm
        );
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // Redirigir a la página de login con error de autenticación si es necesario
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
