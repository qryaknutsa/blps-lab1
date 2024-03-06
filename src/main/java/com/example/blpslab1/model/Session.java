package com.example.blpslab1.model;

import com.example.blpslab1.service.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Session {
    @Id
    private String id;
    private Role role;
    private String username;
    private Boolean subscription;


    public Session(Role role, String username, Boolean subscription){
        this.role = role;
        this.username = username;
        this.subscription = subscription;
    }

    public Session(Role role, String username){
        this.role = role;
        this.username = username;
    }

    public Session(Role role){
        this.role = role;
    }
}
