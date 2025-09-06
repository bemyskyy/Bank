package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    public User createAdminUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ADMIN);

        return userRepository.save(user);
    }
}
