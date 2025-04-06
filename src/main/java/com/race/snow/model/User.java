package com.race.snow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El nombre es obligatorio")
    private String name;

    @NotEmpty(message = "El nombre de usuario es obligatorio")
    @Column(unique = true)
    private String username;

    @NotEmpty(message = "La contraseña es obligatoria")
    private String password;

    @NotEmpty(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Column(unique = true)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    // Para simplificar la verificación de roles
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
