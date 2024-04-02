package com.example.blpslab1.auth.service;

import com.example.blpslab1.auth.dto.AuthenticationRequest;
import com.example.blpslab1.auth.dto.AuthenticationResponse;
import com.example.blpslab1.auth.dto.RegisterRequest;
import com.example.blpslab1.config.JwtService;
import com.example.blpslab1.model.subModel.Role;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.UserRepo;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepo repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager manager;

    public AuthenticationResponse register(RegisterRequest request) {

        User user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()), Role.USER);
        repo.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();

    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        //TODO: exception
        var user = repo.findUserByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);

//        SecurityContextHolder.getContext().setAuthentication(new Authentication() {});
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
