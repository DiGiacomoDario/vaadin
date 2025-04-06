package com.race.snow.repository;

import com.race.snow.model.Event;
import com.race.snow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUser(User user);
    List<Event> findByUserAndStartBetween(User user, LocalDateTime start, LocalDateTime end);
    List<Event> findByStartBefore(LocalDateTime dateTime);
}
