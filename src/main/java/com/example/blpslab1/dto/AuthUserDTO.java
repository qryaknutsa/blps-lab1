package com.example.blpslab1.dto;

import com.example.blpslab1.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserDTO {
    private String username;
    private String password;


    public AuthUserDTO(User user){
        this.username = user.getUsername();
        this.password = user.getPassword();
    }

}
