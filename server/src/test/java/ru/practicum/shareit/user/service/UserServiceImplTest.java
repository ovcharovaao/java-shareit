package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail("user" + id + "@mail.com");
        return user;
    }

    private UserDto createTestUserDto(Long id) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName("User " + id);
        dto.setEmail("user" + id + "@mail.com");
        return dto;
    }

    @Test
    void createUser_ShouldReturnSavedUser() {
        UserDto inputDto = createTestUserDto(null);
        User userToSave = createTestUser(null);
        User savedUser = createTestUser(1L);
        UserDto expectedDto = createTestUserDto(1L);

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userMapper.toUser(any(UserDto.class))).thenReturn(userToSave);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserDto(any(User.class))).thenReturn(expectedDto);

        UserDto result = userService.createUser(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).existsByEmailIgnoreCase(inputDto.getEmail());
        verify(userRepository).save(userToSave);
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowConflictException() {
        UserDto inputDto = createTestUserDto(null);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(inputDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldUpdateNameAndEmail() {
        Long userId = 1L;
        User existingUser = createTestUser(userId);
        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("new@mail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toUserDto(any(User.class))).thenReturn(updateDto);

        UserDto result = userService.updateUser(userId, updateDto);

        assertEquals("Updated Name", result.getName());
        assertEquals("new@mail.com", result.getEmail());
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_WithExistingEmail_ShouldThrowConflictException() {
        Long userId = 1L;
        User existingUser = createTestUser(userId);
        UserDto updateDto = new UserDto();
        updateDto.setEmail("existing@mail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUser(userId, updateDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WithSameEmail_ShouldNotCheckExistence() {
        Long userId = 1L;
        User existingUser = createTestUser(userId);
        UserDto updateDto = new UserDto();
        updateDto.setEmail(existingUser.getEmail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toUserDto(any(User.class))).thenReturn(updateDto);

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
    }

    @Test
    void getUser_ExistingId_ShouldReturnUser() {
        Long userId = 1L;
        User user = createTestUser(userId);
        UserDto expectedDto = createTestUserDto(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(any(User.class))).thenReturn(expectedDto);

        UserDto result = userService.getUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void getUser_NonExistingId_ShouldThrowNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUser(userId));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        User user1 = createTestUser(1L);
        User user2 = createTestUser(2L);
        List<User> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toUserDto(any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    return createTestUserDto(u.getId());
                });

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void deleteUser_ExistingId_ShouldDeleteUser() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_NonExistingId_ShouldThrowNotFoundException() {
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(any());
    }
}
