package com.example.blpslab1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Admin {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String password;

    public Admin(String username, String password){
        this.username = username;
        this.password = password;
    }

}
