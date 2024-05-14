package com.example.blpslab1.model;

import com.example.blpslab1.subModel.FileType;
import com.example.blpslab1.subModel.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ownership")
public class Ownership {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_login")
    private String userLogin;
    @Column(name = "file_id")
    private String fileId;
    @Column(name = "filename")
    private String filename;
    @Column(name = "file_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType type;



    public Ownership(String userLogin, String fileId, FileType type, String filename){
        this.userLogin = userLogin;
        this.fileId = fileId;
        this.type = type;
        this.filename = filename;
    }
}
