package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createAdminUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long id, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // запретить удалять самого себя
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new RuntimeException("You cannot delete yourself");
        }

        // запретить удалять админов
        if (targetUser.getRole() == Role.ADMIN) {
            throw new RuntimeException("You cannot delete another admin");
        }

        userRepository.delete(targetUser);
    }
}