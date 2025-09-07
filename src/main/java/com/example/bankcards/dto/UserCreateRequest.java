package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserCreateRequest {
    @NotBlank(message = "Username обязателен")
    @Size(min = 3, max = 50, message = "Username должен быть от 3 до 100 символов")
    private String username;

    @NotBlank(message = "Password обязателен")
    @Size(min = 6, max = 100, message = "Password должен быть от 6 до 255 символов")
    private String password;

    @NotNull(message = "Role обязателен")
    private Role role;
}