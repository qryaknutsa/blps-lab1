package com.example.blpslab1.controller;

import com.example.blpslab1.dto.RegUserDTO;
import com.example.blpslab1.exceptions.*;
import com.example.blpslab1.model.User;
import com.example.blpslab1.model.subModel.Wallet;
import com.example.blpslab1.service.UserService;

import javax.transaction.SystemException;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
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
    public ResponseEntity<?> createUser(@RequestBody RegUserDTO user) {
        try {
            userService.saveUser(user);
            return ResponseEntity.ok().body("Пользователь успешно создан");
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
    public ResponseEntity<?> getUser(@PathVariable @Nullable String username) {
        try {
            User loggedUser = getLoggedUser();
            if (username == null)
                return ResponseEntity.ok().body(loggedUser);
            else {
                if (loggedUser.getRoleName() == ADMIN) {
                    User user = userService.getUser(username);
                    return ResponseEntity.ok().body(user);
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (NullPointerException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = PUT, value = {"/change-password", "/change-password/{username}"})
    public ResponseEntity<?> changeAdminPassword(@PathVariable @Nullable String username, @RequestBody @NonNull User user) {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.changePassword(user);
                return ResponseEntity.ok().body("Пароль успешно изменено");
            } else {
                if (loggedUser.getRoleName() == ADMIN) {
                    userService.changePassword(user);
                    return ResponseEntity.ok().body("Пароль успешно изменено");
                } else
                    return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | UserAlreadyExistsException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }

    }

    @RequestMapping(method = PUT, value = {"/change-username/{old_username}"})
    public ResponseEntity<?> changeAdminUsername(@PathVariable @NonNull String old_username, @RequestBody @NonNull User user) {
        try {
            User loggedUser = getLoggedUser();
            if (old_username.equals(loggedUser.getUsername())) {
                userService.changeUsername(old_username, user);
                return ResponseEntity.ok().body("Имя пользователя успешно изменено");
            } else {
                if (loggedUser.getRoleName() == ADMIN) {
                    userService.changeUsername(old_username, user);
                    return ResponseEntity.ok().body("Имя пользователя успешно изменено");
                } else
                    return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | UserAlreadyExistsException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = DELETE, value = {"/delete", "/delete/{username}"})
    public ResponseEntity<?> delete(@PathVariable @Nullable String username) {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.delete(loggedUser.getUsername());
                return ResponseEntity.ok().body("Аккаунт " + loggedUser.getUsername() + " успешно удалён");
            } else {
                if (loggedUser.getRoleName() == ADMIN) {
                    userService.delete(username);
                    return ResponseEntity.ok().body("Аккаунт " + username + " успешно удалён");
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = PUT, value = {"/buy-sub", "/buy-sub/{username}"})
    public ResponseEntity<?> buySub(@PathVariable @Nullable String username) throws SystemException {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.buySub(loggedUser.getUsername());
                return ResponseEntity.ok().body("Подписка на аккаунте " + loggedUser.getUsername() + " успешно оформлена");
            } else {
                if (loggedUser.getRoleName() == ADMIN) {
                    userService.buySub(username);
                    return ResponseEntity.ok().body("Подписка на аккаунте " + username + " успешно оформлена");
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | OutOfBalanceException |
                 SubAlreadyExistsException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = PUT, value = {"/cancel-sub", "/cancel-sub/{username}"})
    public ResponseEntity<?> cancelSub(@PathVariable @Nullable String username) throws SystemException {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.cancelSub(loggedUser.getUsername());
                return ResponseEntity.ok().body("Подписка на аккаунте " + loggedUser.getUsername() + " успешно отменена");
            } else {
                if (loggedUser.getRoleName() == ADMIN) {
                    userService.cancelSub(username);
                    return ResponseEntity.ok().body("Подписка на аккаунте " + username + " успешно отменена");
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | OutOfBalanceException |
                 SubAlreadyExistsException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }

    }


    @RequestMapping(method = PUT, value = {"/put-money", "/put-money/{username}"})
    public ResponseEntity<?> putMoney(@PathVariable @Nullable String username, @RequestBody @NonNull Wallet wallet) throws SystemException {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.putMoney(loggedUser.getUsername(), wallet.getSum());
                return ResponseEntity.ok().body("Кошелёк успешно пополнился в размере " + wallet.getSum() + " на счету " + loggedUser.getUsername());
            } else {
                if (loggedUser.getRoleName() == ADMIN) {
                    userService.putMoney(username, wallet.getSum());
                    return ResponseEntity.ok().body("Кошелёк успешно пополнился в размере " + wallet.getSum() + " на счету " + username);
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | OutOfBalanceException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = PUT, value = {"/withdraw-money", "/withdraw-money/{username}"})
    public ResponseEntity<?> withdrawMoney(@PathVariable @Nullable String username, @RequestBody @NonNull Wallet wallet) throws Exception {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                userService.withdrawMoney(loggedUser.getUsername(), wallet.getSum());
                return ResponseEntity.ok().body("Деньги в размере " + wallet.getSum() + " успешно снялись со счёта " + loggedUser.getUsername());
            } else {
                if (loggedUser.getRoleName() == ADMIN) {
                    userService.withdrawMoney(username, wallet.getSum());
                    return ResponseEntity.ok().body("Деньги в размере " + wallet.getSum() + " успешно снялись со счёта " + username);
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | OutOfBalanceException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    private User getLoggedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
