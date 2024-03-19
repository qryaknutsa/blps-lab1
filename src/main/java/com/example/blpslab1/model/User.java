package com.example.blpslab1.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String password;
    private Boolean subscription;
    private Double wallet = (double) 0;

    public User(String username, String password, Boolean subscription, Double wallet){
        this.username = username;
        this.password = password;
        this.subscription = subscription;
        this.wallet = wallet;
    }

}
