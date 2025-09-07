package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.UserUpdateRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        UserResponse resp = new UserResponse(1L, "admin", Role.ADMIN);
        when(userService.getAllUsers()).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        UserCreateRequest req = new UserCreateRequest("newuser", "pass123", Role.USER);
        UserResponse resp = new UserResponse(2L, "newuser", Role.USER);

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    void getUser_ShouldReturnUser() throws Exception {
        UserResponse resp = new UserResponse(3L, "some", Role.USER);
        when(userService.getUser(3L)).thenReturn(resp);

        mockMvc.perform(get("/api/users/{id}", 3L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("some"));

        verify(userService).getUser(3L);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setUsername("updated");
        req.setPassword("newpass");
        req.setRole(Role.ADMIN);

        UserResponse resp = new UserResponse(4L, "updated", Role.ADMIN);
        when(userService.updateUser(eq(4L), any(UserUpdateRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/api/users/{id}", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.username").value("updated"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).updateUser(eq(4L), any(UserUpdateRequest.class));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        UserDetails userDetails = User.withUsername("admin")
                .password("password")
                .roles("ADMIN")
                .build();

        doNothing().when(userService).deleteUser(eq(5L), eq("admin"));

        mockMvc.perform(delete("/api/users/{id}", 5L)
                        .with(user(userDetails))
                        .principal(() -> "admin"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(eq(5L), eq("admin"));
    }
}