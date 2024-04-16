package com.example.blpslab1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stored_file")
public class StoredFile {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "data", nullable = false)
    private String data;
    @Column(name = "username", nullable = false)
    private String username;

    public StoredFile(String title, String data, String username) {
        this.title = title;
        this.data = data;
        this.username = username;
    }

}
