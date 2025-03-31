package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final Map<Long, Item> itemStore = new HashMap<>();
    private Long idCounter = 1L;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Запрос на создание вещи от пользователя id={}: {}", userId, itemDto);

        User owner = userMapper.toUser(userService.getUser(userId));
        if (owner == null) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.warn("Название вещи не может быть пустым");
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.warn("Описание вещи не может быть пустым");
            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            log.warn("Поле available обязательно");
            throw new ValidationException("Поле available обязательно");
        }

        Item item = itemMapper.toItem(itemDto);
        item.setId(idCounter++);
        item.setOwner(owner);
        itemStore.put(item.getId(), item);

        log.info("Вещь создана: {}", item);
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Запрос на обновление вещи id={} от пользователя id={}: {}", itemId, userId, itemDto);

        Item existing = itemStore.get(itemId);
        if (existing == null) {
            log.error("Вещь с id={} не найдена", itemId);
            throw new NotFoundException("Вещь не найдена");
        }

        if (!existing.getOwner().getId().equals(userId)) {
            log.error("Пользователь с id={} не является владельцем вещи id={}", userId, itemId);
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existing.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existing.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        log.info("Вещь id={} обновлена: {}", itemId, existing);
        return itemMapper.toItemDto(existing);
    }

    @Override
    public ItemDto getItem(Long userId, Long itemId) {
        log.info("Запрос на получение вещи id={} от пользователя id={}", itemId, userId);

        Item item = itemStore.get(itemId);
        if (item == null) {
            log.error("Вещь с id={} не найдена", itemId);
            throw new NotFoundException("Вещь не найдена");
        }

        log.info("Вещь найдена: {}", item);
        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByUser(Long userId) {
        log.info("Запрос на получение всех вещей пользователя id={}", userId);

        List<ItemDto> result = new ArrayList<>();
        for (Item item : itemStore.values()) {
            if (item.getOwner().getId().equals(userId)) {
                result.add(itemMapper.toItemDto(item));
            }
        }

        log.info("Найдено {} вещей для пользователя id={}", result.size(), userId);
        return result;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Запрос на поиск вещей по тексту: '{}'", text);

        if (text == null || text.isBlank()) {
            log.warn("Поиск с пустым текстом, возвращается пустой список");
            return Collections.emptyList();
        }

        List<ItemDto> result = new ArrayList<>();
        for (Item item : itemStore.values()) {
            if (Boolean.TRUE.equals(item.getAvailable()) &&
                    (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                            item.getDescription().toLowerCase().contains(text.toLowerCase()))) {
                result.add(itemMapper.toItemDto(item));
            }
        }

        log.info("Найдено {} вещей", result.size());
        return result;
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        log.info("Запрос на удаление вещи id={} от пользователя id={}", itemId, userId);

        Item item = itemStore.get(itemId);
        if (item == null) {
            log.error("Вещь с id={} не найдена", itemId);
            throw new NotFoundException("Вещь не найдена");
        }

        if (!item.getOwner().getId().equals(userId)) {
            log.error("Пользователь с id={} не является владельцем вещи id={}", userId, itemId);
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        itemStore.remove(itemId);
        log.info("Вещь с id={} удалена", itemId);
    }
}
