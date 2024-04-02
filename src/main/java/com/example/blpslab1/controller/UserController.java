package com.example.blpslab1.controller;

import com.example.blpslab1.model.User;
import com.example.blpslab1.dto.ResponseStatus;
import com.example.blpslab1.model.subModel.Wallet;
import com.example.blpslab1.service.UserService;

import javax.transaction.SystemException;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.blpslab1.dto.ResponseStatus.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserController {
    private final UserService userService;


    //USER PART

    @GetMapping("/user")
    public ResponseEntity<User> getUser() {
        try {
            String username = getLoggedUsername();
            User user = userService.getUser(username);
            return ResponseEntity.ok().body(user);
        } catch (NullPointerException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @DeleteMapping("/user/delete")
    public ResponseEntity<Void> delete() {
        String username = getLoggedUsername();
        ResponseStatus response = userService.delete(username);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/user/buy-sub")
    public ResponseEntity<Void> buySub() throws SystemException {
        String username = getLoggedUsername();
        ResponseStatus response = userService.buySub(username);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/user/cancel-sub")
    public ResponseEntity<Void> cancelSub() throws SystemException {
        String username = getLoggedUsername();
        ResponseStatus response = userService.cancelSub(username);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }


    @PutMapping("/user/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody User user) {
        ResponseStatus response = userService.changePassword(user);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/user/change-username")
    public ResponseEntity<Void> changeUsername(@RequestBody User user) {
        String username = getLoggedUsername();
        ResponseStatus response = userService.changeUsername(username, user);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }


    @PutMapping("/user/put-money")
    public ResponseEntity<Void> putMoney(@RequestBody Wallet wallet) throws SystemException {
        String username = getLoggedUsername();
        ResponseStatus response = userService.putMoney(username, wallet.getSum());
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/user/withdraw-money")
    public ResponseEntity<Void> withdrawMoney(@RequestBody Wallet wallet) throws SystemException {
        String username = getLoggedUsername();
        ResponseStatus response = userService.withdrawMoney(username, wallet.getSum());
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }



    // ADMIN PART

    @PostMapping("/admin/create")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            ResponseStatus status = userService.saveUser(user);
            if (status == ALREADY_EXISTS) throw new NullPointerException();
            return ResponseEntity.ok().body(user);
        } catch (NullPointerException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/admin/user/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        try {
            User user = userService.getUser(username);
            return ResponseEntity.ok().body(user);
        } catch (NullPointerException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/admin/all-users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok().body(users);
        } catch (NullPointerException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @DeleteMapping("/admin/delete/{username}")
    public ResponseEntity<Void> delete(@PathVariable String username) {
        ResponseStatus response = userService.delete(username);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/admin/buy-sub/{username}")
    public ResponseEntity<Void> buySub(@PathVariable String username) throws SystemException {
        ResponseStatus response = userService.buySub(username);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/admin/cancel-sub/{username}")
    public ResponseEntity<Void> cancelSub(@PathVariable String username) throws SystemException {
        ResponseStatus response = userService.cancelSub(username);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }


    @PutMapping("/admin/change-password")
    public ResponseEntity<Void> changeAdminPassword(@RequestBody User user) {
        ResponseStatus response = userService.changePassword(user);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/admin/change-username")
    public ResponseEntity<Void> changeAdminUsername(@RequestBody User user) {
        String username = getLoggedUsername();
        ResponseStatus response = userService.changeUsername(username, user);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/admin/put-money/{username}")
    public ResponseEntity<Void> putMoney(@PathVariable String username, @RequestBody Wallet wallet) throws SystemException {
        ResponseStatus response = userService.putMoney(username, wallet.getSum());
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @PutMapping("/admin/withdraw-money/{username}")
    public ResponseEntity<Void> withdrawMoney(@PathVariable String username, @RequestBody Wallet wallet) throws SystemException {
        ResponseStatus response = userService.withdrawMoney(username, wallet.getSum());
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }


    private String getLoggedUsername() {
        User a = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return a.getUsername();
    }

}
