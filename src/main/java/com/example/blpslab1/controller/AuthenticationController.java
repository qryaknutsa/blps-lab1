package com.example.blpslab1.controller;

import com.example.blpslab1.dto.AuthUserDTO;
import com.example.blpslab1.service.AuthenticationService;
import com.example.blpslab1.dto.RegUserDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@RequestBody AuthUserDTO request){
        return ResponseEntity.ok(service.register(request));
    }

}

