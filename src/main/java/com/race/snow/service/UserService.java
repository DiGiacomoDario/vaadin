package com.race.snow.service;

import com.race.snow.model.User;
import com.race.snow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        if (user.getId() == null) {
            // New user, encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // Existing user, check if password changed
            userRepository.findById(user.getId()).ifPresent(existingUser -> {
                if (!user.getPassword().equals(existingUser.getPassword())) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                }
            });
        }
        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void initializeDefaultUsers() {
        if (!existsByUsername("admin")) {
            User admin = new User("admin", "admin", "Administrator", "admin@example.com");
            admin.addRole("admin");
            save(admin);
        }
        
        if (!existsByUsername("gerente")) {
            User manager = new User("gerente", "gerente", "Gerente Demo", "gerente@example.com");
            manager.addRole("gerente");
            save(manager);
        }
    }
}
