package com.example.blpslab1.dto;

import com.example.blpslab1.subModel.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegUserDTO {
    private String username;
    private String password;
    private Role role;

}
