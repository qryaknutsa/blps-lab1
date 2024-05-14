package com.example.blpslab1.service;

import com.example.blpslab1.dto.AuthUserDTO;
import com.example.blpslab1.config.SecurityConfig;
import com.example.blpslab1.model.User;
import com.example.blpslab1.subModel.Role;
import com.example.blpslab1.repo.UserRepo;


import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepo repo;
    private final PasswordEncoder passwordEncoder;

    public AuthUserDTO register(AuthUserDTO request) {
        String username = request.getUsername();
        String password = request.getPassword();
        if (username == null || password == null) {
            throw new IllegalArgumentException("Login and password cannot be null");
        }
        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Login and password cannot be empty");
        }
        if (repo.findUserByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Login already exists");
        }
        User user = new User(username, SecurityConfig.passwordEncoder().encode(password), Role.USER);
        repo.save(user);
        return new AuthUserDTO(user);

    }

}
