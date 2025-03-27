package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
    }

    @Test
    void createUser() {
        UserDto userDto = new UserDto(null, "User", "user@example.com");
        UserDto createdUser = userService.createUser(userDto);

        assertNotNull(createdUser.getId());
        assertEquals("User", createdUser.getName());
        assertEquals("user@example.com", createdUser.getEmail());
    }

    @Test
    void updateUser() {
        UserDto userDto = userService.createUser(new UserDto(null, "User", "user@example.com"));
        UserDto updatedUser = userService.updateUser(userDto.getId(), new UserDto(null,
                "UserUpdated", null));

        assertEquals("UserUpdated", updatedUser.getName());
        assertEquals("user@example.com", updatedUser.getEmail());
    }

    @Test
    void getUser() {
        UserDto userDto = userService.createUser(new UserDto(null, "User", "user@example.com"));
        UserDto foundUser = userService.getUser(userDto.getId());

        assertEquals("User", foundUser.getName());
        assertEquals("user@example.com", foundUser.getEmail());
    }

    @Test
    void getAllUsers() {
        userService.createUser(new UserDto(null, "User1", "user1@example.com"));
        userService.createUser(new UserDto(null, "User2", "user2@example.com"));

        List<UserDto> users = userService.getAllUsers();
        assertEquals(2, users.size());
    }

    @Test
    void deleteUser() {
        UserDto userDto = userService.createUser(new UserDto(null, "User", "user@example.com"));
        userService.deleteUser(userDto.getId());
        assertThrows(NotFoundException.class, () -> userService.getUser(userDto.getId()));
    }

    @Test
    void createUserWithSameEmail() {
        userService.createUser(new UserDto(null, "User1", "user1@example.com"));
        assertThrows(ConflictException.class, () ->
                userService.createUser(new UserDto(null, "User2", "user1@example.com")));
    }

    @Test
    void updateUserWithNonExistentId() {
        assertThrows(NotFoundException.class, () ->
                userService.updateUser(999L, new UserDto(null, "User", "user@example.com")));
    }

    @Test
    void getUserWithNonExistentId() {
        assertThrows(NotFoundException.class, () -> userService.getUser(999L));
    }
}
