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

    // ---- 2. FIND USER BY EMAIL ----
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ---- 3. CHECK LOGIN CREDENTIALS ----
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // ---- 4. MARK EMAIL AS VERIFIED ----
    public void markEmailAsVerified(User user) {
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    // ---- 5. UPDATE PASSWORD ----
    public void updatePassword(User user, String newRawPassword) {
        String hashedPassword = passwordEncoder.encode(newRawPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    // ---- 6. CHECK IF EMAIL IS VERIFIED (still important - keeps your core requirement intact) ----
    public boolean isAccountUsable(User user) {
        return user.isEmailVerified();
    }
}