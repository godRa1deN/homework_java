package com.programm.User;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserService {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User setUserData(User user, String username, String email, String password, String role) {
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        return user;
    }

    public boolean isPasswordCorrect(String password, String passwordHash) {
        return passwordEncoder.matches(password, passwordHash);
    }
}