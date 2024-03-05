package com.example.blpslab1.model;

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
    private String username;
    private Boolean subscription;


    public Session(String username, Boolean subscription){
        this.username = username;
        this.subscription = subscription;

    }
}
