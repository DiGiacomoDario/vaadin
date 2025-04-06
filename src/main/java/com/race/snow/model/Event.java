package com.race.snow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El título es obligatorio")
    private String title;

    private String description;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime start;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime end;

    private String color;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
