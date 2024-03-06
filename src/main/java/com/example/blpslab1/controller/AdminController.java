package com.example.blpslab1.controller;

import com.example.blpslab1.model.Admin;
import com.example.blpslab1.model.User;
import com.example.blpslab1.service.AdminService;
import com.example.blpslab1.service.ResponseStatus;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.blpslab1.service.ResponseStatus.*;

@RestController
@RequestMapping("api/admin")
@AllArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> list = adminService.getAllUsers();
        return ResponseEntity.ok().body(list);

    }

    @PostMapping("/signin")
    public ResponseEntity<Void> signIn(@RequestBody Admin admin) {
        ResponseStatus response = adminService.signIn(admin);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else if (response == WRONG_PASSWORD)
            return ResponseEntity.internalServerError().build();
        else return ResponseEntity.notFound().build();

    }


    @PostMapping("/create-user")
    public ResponseEntity<Void> createUser(@RequestBody User user) {
        ResponseStatus response = adminService.createUser(user);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();

    }

    @PostMapping("/create-admin")
    public ResponseEntity<Void> createAdmin(@RequestBody Admin admin) {
        ResponseStatus response = adminService.createAdmin(admin);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @GetMapping("/get-user/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        User user = adminService.getUser(username);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/get-admin/{username}")
    public ResponseEntity<Admin> getAdmin(@PathVariable String username) {
        Admin admin = adminService.getAdmin(username);
        return ResponseEntity.ok().body(admin);
    }

    @DeleteMapping("/del-user/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        ResponseStatus response = adminService.deleteUser(username);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @DeleteMapping("/del-admin/{username}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String username) {
        ResponseStatus response = adminService.deleteAdmin(username);
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

//    @PutMapping("{username}")
//    public ResponseEntity<Void> changeSub(@PathVariable String username) {
//        ResponseStatus response = adminService.updateSub(username);
//        if (response == GOOD)
//            return ResponseEntity.ok().build();
//        else return ResponseEntity.internalServerError().build();
//    }


    @GetMapping("/exit")
    public ResponseEntity<User> exit() {
        adminService.exit();
        return ResponseEntity.ok().build();
    }

}
