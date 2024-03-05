package com.example.blpslab1.controller;

import com.example.blpslab1.model.User;
import com.example.blpslab1.service.ResponseStatus;
import com.example.blpslab1.service.UserService;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

import static com.example.blpslab1.service.ResponseStatus.*;

@RestController
@RequestMapping("api/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok().body(userService.getAllUsers());
    }

    @PostMapping("/signin")
    public ResponseEntity<Void> signIn(@RequestBody User user) {
        ResponseStatus response = userService.signIn(user);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else if(response == WRONG_PASSWORD)
            return ResponseEntity.internalServerError().build();
        else return ResponseEntity.notFound().build();

    }


    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody User user) {
        ResponseStatus response = userService.signUp(user);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @GetMapping("{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        try{
            User user = userService.getUser(username);
            return ResponseEntity.ok().body(user);
        } catch(NullPointerException e){
            return ResponseEntity.internalServerError().body(null);
        }
    }
    @DeleteMapping("{username}")
    public ResponseEntity<Void> delete(@PathVariable String username){
        ResponseStatus response = userService.delete(username);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("{username}")
    public ResponseEntity<Void> changeSub(@PathVariable String username){
        ResponseStatus response = userService.updateSub(username);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }


    @GetMapping("/exit")
    public ResponseEntity<User> exit() {
        userService.exit();
        return ResponseEntity.ok().build();
    }

}
