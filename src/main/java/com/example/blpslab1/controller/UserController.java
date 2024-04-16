package com.example.blpslab1.controller;

import com.example.blpslab1.exceptions.*;
import com.example.blpslab1.model.User;
import com.example.blpslab1.model.subModel.Role;
import com.example.blpslab1.model.subModel.Wallet;
import com.example.blpslab1.service.UserService;

import javax.transaction.SystemException;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.blpslab1.model.subModel.Role.ADMIN;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            userService.saveUser(user);
            return ResponseEntity.ok().body(user);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok().body(users);
        } catch (NullPointerException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = GET, value = {"", "/{username}"})
    public ResponseEntity<?> getUser(@PathVariable String username) {
        try {
            User loggedUser = getLoggedUser();
            if (username == null)
                return ResponseEntity.ok().body(loggedUser);
            else {
                if (loggedUser.getRole() == ADMIN) {
                    User user = userService.getUser(username);
                    return ResponseEntity.ok().body(user);
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (NullPointerException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());        }
    }

    @RequestMapping(method = PUT, value = {"/change-password", "/change-password/{username}"})
    public ResponseEntity<?> changeAdminPassword(@PathVariable String username, @RequestBody User user) {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.changePassword(user);
                return ResponseEntity.ok().build();
            } else {
                if (loggedUser.getRole() == ADMIN) {
                    userService.changePassword(user);
                    return ResponseEntity.ok().build();
                } else
                    return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | UserAlreadyExistsException  e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }

    }

    @RequestMapping(method = PUT, value = {"/change-username/{old_username}"})
    public ResponseEntity<?> changeAdminUsername(@PathVariable String old_username, @RequestBody User user) {
        try {
            User loggedUser = getLoggedUser();
            if (old_username.equals(loggedUser.getUsername())) {
                userService.changeUsername(old_username, user);
                return ResponseEntity.ok().build();
            } else {
                if (loggedUser.getRole() == ADMIN) {
                    userService.changeUsername(old_username, user);
                    return ResponseEntity.ok().build();
                } else
                    return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | UserAlreadyExistsException  e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = DELETE, value = {"/delete", "/delete/{username}"})
    public ResponseEntity<?> delete(@PathVariable String username) {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.delete(loggedUser.getUsername());
                return ResponseEntity.ok().body(loggedUser);
            } else {
                if (loggedUser.getRole() == ADMIN) {
                    userService.delete(username);
                    return ResponseEntity.ok().build();
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = PUT, value = {"/buy-sub", "/buy-sub/{username}"})
    public ResponseEntity<?> buySub(@PathVariable String username) throws SystemException {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.buySub(loggedUser.getUsername());
                return ResponseEntity.ok().body(loggedUser);
            } else {
                if (loggedUser.getRole() == ADMIN) {
                    userService.buySub(username);
                    return ResponseEntity.ok().build();
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | OutOfBalanceException | TransactionFailedException |
                 SubAlreadyExistsException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = PUT, value = {"/cancel-sub", "/cancel-sub/{username}"})
    public ResponseEntity<?> cancelSub(@PathVariable String username) throws SystemException {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.cancelSub(loggedUser.getUsername());
                return ResponseEntity.ok().body(loggedUser);
            } else {
                if (loggedUser.getRole() == ADMIN) {
                    userService.cancelSub(username);
                    return ResponseEntity.ok().build();
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | OutOfBalanceException | TransactionFailedException |
                 SubAlreadyExistsException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }

    }


    @RequestMapping(method = PUT, value = {"/put-money", "/put-money/{username}"})
    public ResponseEntity<?> putMoney(@PathVariable String username, @RequestBody Wallet wallet) throws SystemException {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.putMoney(loggedUser.getUsername(), wallet.getSum());
                return ResponseEntity.ok().body(loggedUser);
            } else {
                if (loggedUser.getRole() == ADMIN) {
                    userService.putMoney(username, wallet.getSum());
                    return ResponseEntity.ok().build();
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | OutOfBalanceException | TransactionFailedException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = PUT, value = {"/withdraw-money", "/withdraw-money/{username}"})
    public ResponseEntity<?> withdrawMoney(@PathVariable String username, @RequestBody Wallet wallet) throws Exception {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.withdrawMoney(loggedUser.getUsername(), wallet.getSum());
                return ResponseEntity.ok().body(loggedUser);
            } else {
                if (loggedUser.getRole() == ADMIN) {
                    userService.withdrawMoney(username, wallet.getSum());
                    return ResponseEntity.ok().build();
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | OutOfBalanceException | TransactionFailedException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    private User getLoggedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
