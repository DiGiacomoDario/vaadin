package com.race.snow.repository;

import com.race.snow.model.Event;
import com.race.snow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUser(User user);
    List<Event> findByUserAndStartBetween(User user, LocalDateTime start, LocalDateTime end);
}
