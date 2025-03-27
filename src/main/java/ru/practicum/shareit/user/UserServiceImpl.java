package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final Map<Long, User> userStore = new HashMap<>();
    private Long idCounter = 1L;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Запрос на создание пользователя: {}", userDto);

        for (User user : userStore.values()) {
            if (user.getEmail().equalsIgnoreCase(userDto.getEmail())) {
                log.warn("Email {} уже используется", userDto.getEmail());
                throw new ConflictException("Пользователь с таким email уже существует");
            }
        }

        User user = UserMapper.toUser(userDto);
        user.setId(idCounter++);
        userStore.put(user.getId(), user);

        log.info("Пользователь создан: {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Запрос на обновление пользователя id={}: {}", userId, userDto);

        User existing = userStore.get(userId);
        if (existing == null) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        if (userDto.getEmail() != null && !existing.getEmail().equalsIgnoreCase(userDto.getEmail())) {
            for (User user : userStore.values()) {
                if (user.getEmail().equalsIgnoreCase(userDto.getEmail())) {
                    log.warn("Email {} уже используется", userDto.getEmail());
                    throw new ConflictException("Пользователь с таким email уже существует");
                }
            }
            existing.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }

        log.info("Пользователь обновлен: {}", existing);
        return UserMapper.toUserDto(existing);
    }

    @Override
    public UserDto getUser(Long userId) {
        log.info("Запрос на получение пользователя id={}", userId);

        User user = userStore.get(userId);
        if (user == null) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        log.info("Пользователь найден: {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        return userStore.values().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Запрос на удаление пользователя id={}", userId);

        if (!userStore.containsKey(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        userStore.remove(userId);
        log.info("Пользователь с id={} удален", userId);
    }
}
