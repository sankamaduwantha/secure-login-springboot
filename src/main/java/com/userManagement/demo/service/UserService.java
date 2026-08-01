package com.userManagement.demo.service;

import com.userManagement.demo.model.User;
import com.userManagement.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String fullName, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(fullName, email, hashedPassword);
        user.setEmailVerified(false);

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void markEmailAsVerified(User user) {
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    public boolean isAccountUsable(User user) {
        return user.isEmailVerified();
    }
}