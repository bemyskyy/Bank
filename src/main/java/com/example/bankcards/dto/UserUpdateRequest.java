package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @Size(min = 3, max = 50, message = "Username должен быть от 3 до 100 символов")
    private String username;

    @Size(min = 6, max = 100, message = "Password должен быть от 6 до 255 символов")
    private String password;

    private Role role;
}