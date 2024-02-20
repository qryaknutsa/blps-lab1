package com.example.blpslab1.controller;

import com.example.blpslab1.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.List;


import com.example.blpslab1.repo.UserRepo;

@RestController
@RequestMapping("api/user")
public class UserController {
    private final UserRepo userRepo;
    private final MessageDigest md = MessageDigest.getInstance("SHA-512");

    @Autowired
    public UserController(UserRepo userRepo) throws NoSuchAlgorithmException {
        this.userRepo = userRepo;
    }

    @PostMapping("signin/")
    public ResponseEntity<Void> signIn(@RequestBody User reqUser) {
        User realUser;
        try {
            realUser = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(reqUser.getUsername())).findFirst().get();
            String reqPass = encryptPassword(reqUser.getPassword());
            if (realUser.getPassword().equals(reqPass)) return ResponseEntity.status(HttpStatus.OK).build();
            else return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @PostMapping("signup/")
    public ResponseEntity<Void> signUp(@RequestBody User user) {
        try {
            userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(user.getUsername())).findFirst().get();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (NoSuchElementException e) {
            user.setPassword(encryptPassword(user.getPassword()));
            userRepo.save(user);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

    @GetMapping("{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        try{
            User user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
            return ResponseEntity.ok().body(user);
        } catch(NoSuchElementException e){
            return ResponseEntity.badRequest().body(null);
        }
    }
    @DeleteMapping("{username}")
    public ResponseEntity<Void> delete(@PathVariable String username){
        User user;
        try {
            user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
            userRepo.delete(user);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUser() {
        return ResponseEntity.ok().body(userRepo.findAll());
    }

    private String encryptPassword(final String password){
        md.update(password.getBytes());
        byte[] byteBuffer = md.digest();
        StringBuilder strHexString = new StringBuilder();

        for (int i = 0; i < byteBuffer.length; i++) {
            String hex = Integer.toHexString(0xff & byteBuffer[i]);
            if (hex.length() == 1) {
                strHexString.append('0');
            }
            strHexString.append(hex);
        }
        return strHexString.toString();
    }


}
