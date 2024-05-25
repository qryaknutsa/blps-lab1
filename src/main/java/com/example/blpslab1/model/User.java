package com.example.blpslab1.model;

import com.example.blpslab1.subModel.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cloud_user_copy")
public class User implements UserDetails {
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
    @Column(name = "wallet", nullable = false)
    private Double wallet = (double) 0;

    public User(String username, String password, Role roleName, Boolean subscription, Double wallet){
        this.username = username;
        this.password = password;
        this.roleName = roleName;
        this.subscription = subscription;
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


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(roleName.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
