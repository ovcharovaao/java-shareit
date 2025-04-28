package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);

        if (userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        User user = userMapper.toUser(userDto);
        User savedUser = userRepository.save(user);

        return userMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Обновление пользователя ID={} : {}", userId, userDto);

        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (userDto.getEmail() != null && !userDto.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
                throw new ConflictException("Email уже используется");
            }
            existing.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }

        return userMapper.toUserDto(userRepository.save(existing));
    }

    @Override
    public UserDto getUser(Long userId) {
        log.info("Получение пользователя по ID={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID={} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получение списка всех пользователей");

        List<User> users = userRepository.findAll();

        log.info("Найдено {} пользователей", users.size());

        return users.stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID={}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("Попытка удалить несуществующего пользователя с ID={}", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        userRepository.deleteById(userId);
    }
}