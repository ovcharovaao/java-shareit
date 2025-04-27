package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("Маппинг User в UserDto должен работать корректно")
    void toUserDto_shouldMapCorrectly() {
        User user = new User(1L, "UserName", "user@example.com");

        UserDto userDto = userMapper.toUserDto(user);

        assertThat(userDto).isNotNull();
        assertThat(userDto.getId()).isEqualTo(user.getId());
        assertThat(userDto.getName()).isEqualTo(user.getName());
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Маппинг UserDto в User должен работать корректно")
    void toUser_shouldMapCorrectly() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("UserName");
        userDto.setEmail("user@example.com");

        User user = userMapper.toUser(userDto);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userDto.getId());
        assertThat(user.getName()).isEqualTo(userDto.getName());
        assertThat(user.getEmail()).isEqualTo(userDto.getEmail());
    }
}
