package com.example.blpslab1.controller;

import com.example.blpslab1.model.User;
import com.example.blpslab1.model.Wallet;
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

    @GetMapping()
    public ResponseEntity<User> getUser() {
        try{
            User user = userService.getUser();
            return ResponseEntity.ok().body(user);
        } catch(NullPointerException e){
            return ResponseEntity.internalServerError().body(null);
        }
    }
    @DeleteMapping()
    public ResponseEntity<Void> delete(){
        ResponseStatus response = userService.delete();
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/buy-sub")
    public ResponseEntity<Void> buySub(){
        ResponseStatus response = userService.buySub();
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/cancel-sub")
    public ResponseEntity<Void> cancelSub(){
        ResponseStatus response = userService.cancelSub();
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/put-money")
    public ResponseEntity<Void> putMoney(@RequestBody Wallet wallet){
        ResponseStatus response = userService.putMoney(wallet.getSum());
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/withdraw-money")
    public ResponseEntity<Void> withdrawMoney(@RequestBody Wallet wallet){
        ResponseStatus response = userService.withdrawMoney(wallet.getSum());
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
