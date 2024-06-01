package com.example.blpslab1.model;

import com.example.blpslab1.subModel.Role;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cloud_user")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "role_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role roleName;
    @Column(name = "subscription", nullable = false)
    private Boolean subscription = false;
    @Column(name = "sub_date")
    private Date subDate;
    @Column(name = "wallet", nullable = false)
    private Double wallet = (double) 0;

    public User(String username, String password, Role roleName, Boolean subscription, Double wallet){
        this.username = username;
        this.password = password;
        this.roleName = roleName;
        this.subscription = subscription;
        if(subscription) this.subDate = Date.valueOf(LocalDate.now());;
        this.wallet = wallet;
    }

    public User(String username, String password, Role roleName){
        this.username = username;
        this.password = password;
        this.roleName = roleName;
    }

    public User(String username, String password){
        this.username = username;
        this.password = password;
        this.roleName = Role.USER;
    }

}
