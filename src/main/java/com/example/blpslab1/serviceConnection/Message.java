package com.example.blpslab1.serviceConnection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private String title;
    private String data;
    private String username;

    private boolean subscription;
    private String filePath;

    private List<String> list;

    public Message(List<String> list) {
        this.list = list;
    }

    public Message(String username) {
        this.username = username;
    }


    public Message(String username, String filePath, boolean subscription) {
        this.username = username;
        this.filePath = filePath;
        this.subscription = subscription;
    }

    public Message(String title, String username) {
        this.title = title;
        this.username = username;
    }

    public Message(String title, String data, String username) {
        this.title = title;
        this.data = data;
        this.username = username;
    }
}
