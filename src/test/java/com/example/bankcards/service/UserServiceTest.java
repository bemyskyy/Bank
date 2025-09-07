package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.UserUpdateRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_ShouldSuccessfullyCreateUser() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest("newuser", "password", Role.USER);
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        UserResponse result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.getUsername(), result.getUsername());
        assertEquals(Role.USER, result.getRole());
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setRole(Role.USER);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setRole(Role.ADMIN);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUser_ShouldReturnUserWhenExists() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setRole(Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponse result = userService.getUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUser_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getUser(userId));

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateUser_ShouldUpdateUserWhenExists() {
        // Arrange
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updateduser");
        request.setPassword("newpassword");
        request.setRole(Role.ADMIN);

        String encodedPassword = "encodedNewPassword";

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("olduser");
        existingUser.setPassword("oldpassword");
        existingUser.setRole(Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        // Act
        UserResponse result = userService.updateUser(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("updateduser", result.getUsername());
        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        Long userId = 999L;
        UserUpdateRequest request = new UserUpdateRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(userId, request));

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUserWhenConditionsMet() {
        // Arrange
        Long userId = 2L;
        String currentUsername = "admin";

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername(currentUsername);
        currentUser.setRole(Role.ADMIN);

        User targetUser = new User();
        targetUser.setId(userId);
        targetUser.setUsername("todelete");
        targetUser.setRole(Role.USER);

        when(userRepository.findByUsername(currentUsername)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(targetUser));
        doNothing().when(userRepository).delete(targetUser);

        // Act & Assert
        assertDoesNotThrow(() -> userService.deleteUser(userId, currentUsername));
        verify(userRepository, times(1)).findByUsername(currentUsername);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(targetUser);
    }

    @Test
    void deleteUser_ShouldThrowExceptionWhenDeletingSelf() {
        // Arrange
        Long userId = 1L;
        String currentUsername = "user1";

        User currentUser = new User();
        currentUser.setId(userId);
        currentUser.setUsername(currentUsername);
        currentUser.setRole(Role.USER);

        when(userRepository.findByUsername(currentUsername)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(userId, currentUsername));

        assertEquals("Вы не можете удалить себя!", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(currentUsername);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_ShouldThrowExceptionWhenDeletingAdmin() {
        // Arrange
        Long userId = 2L;
        String currentUsername = "admin";

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername(currentUsername);
        currentUser.setRole(Role.ADMIN);

        User targetUser = new User();
        targetUser.setId(userId);
        targetUser.setUsername("admin2");
        targetUser.setRole(Role.ADMIN);

        when(userRepository.findByUsername(currentUsername)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(targetUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(userId, currentUsername));

        assertEquals("Вы не можете удалить админа!", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(currentUsername);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
}