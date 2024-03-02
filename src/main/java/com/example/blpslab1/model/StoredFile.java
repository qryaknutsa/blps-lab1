package com.example.blpslab1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class StoredFile {
    @Id
    private String id;
    @Indexed(unique = true)
    private String title;
    private Binary data;
    private String username;

    public StoredFile(String title, Binary data, String username){
        this.title = title;
        this.data = data;
        this.username = username;
    }

}
