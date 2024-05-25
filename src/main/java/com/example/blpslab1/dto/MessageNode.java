package com.example.blpslab1.dto;

import com.example.blpslab1.subModel.FileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageNode implements Serializable {
    String identifier;
    FileType fileType;
    String name;
    Binary data;


    public MessageNode(String identifier, FileType fileType, String name){
        this.identifier = identifier;
        this.fileType = fileType;
        this.name = name;
    }
}
