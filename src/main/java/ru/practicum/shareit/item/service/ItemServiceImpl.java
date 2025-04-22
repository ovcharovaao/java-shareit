package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем с id={}: {}", userId, itemDto);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.warn("Название вещи не может быть пустым");
            throw new ValidationException("Название вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.warn("Описание вещи не может быть пустым");
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            log.warn("Доступность вещи должна быть указана");
            throw new ValidationException("Доступность вещи должна быть указана");
        }

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);

        return itemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи с id={} пользователем с id={}", itemId, userId);

        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с id={} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        if (!existing.getOwner().getId().equals(userId)) {
            log.warn("Пользователь с id={} не является владельцем вещи с id={}", userId, itemId);
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existing.setName(itemDto.getName());
            log.info("Название вещи обновлено: {}", itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existing.setDescription(itemDto.getDescription());
            log.info("Описание вещи обновлено: {}", itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
            log.info("Статус доступности обновлён: {}", itemDto.getAvailable());
        }

        Item saved = itemRepository.save(existing);
        log.info("Вещь обновлена: {}", saved);
        return itemMapper.toItemDto(saved);
    }

    @Override
    public ItemResponseDto getItem(Long userId, Long itemId) {
        log.info("Получение вещи с id={} для пользователя с id={}", itemId, userId);

        if (userId <= 0 || itemId <= 0) {
            log.warn("Некорректный id пользователя или вещи: userId={}, itemId={}", userId, itemId);
            throw new ValidationException("Некорректный id пользователя или вещи");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с id={} не найдена", itemId);
                    return new NotFoundException("вещь c id " + itemId + " не существует");
                });

        Booking lastBooking = bookingRepository.findPastOwnerBookings(item.getId(), userId, LocalDateTime.now())
                .stream()
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        Booking nextBooking = bookingRepository.findFutureOwnerBookings(item.getId(), userId, LocalDateTime.now())
                .stream()
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        List<CommentDto> commentsDto = commentRepository
                .findByItem_Id(itemId, Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());

        log.info("Вещь с id={} успешно получена", itemId);

        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .lastBooking(lastBooking != null
                        ? BookingShortDto.builder()
                        .id(lastBooking.getId())
                        .bookerId(lastBooking.getBooker().getId())
                        .build()
                        : null)
                .nextBooking(nextBooking != null
                        ? BookingShortDto.builder()
                        .id(nextBooking.getId())
                        .bookerId(nextBooking.getBooker().getId())
                        .build()
                        : null)
                .comments(commentsDto)
                .build();
    }

    @Override
    public List<ItemResponseDto> getItemsByUser(Long userId) {
        log.info("Получение всех вещей пользователя с ID={}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID={} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        List<Item> items = itemRepository.findByOwner_Id(userId,
                Sort.by(Sort.Direction.ASC, "id"));

        log.info("Найдено {} вещей у пользователя с ID={}", items.size(), userId);
        return items.stream()
                .map(item -> getItem(userId, item.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск вещей по запросу: '{}'", text);

        if (text == null || text.isBlank()) {
            log.info("Пустой запрос для поиска вещей — возвращён пустой список");
            return List.of();
        }

        List<ItemDto> result = itemRepository
                .search(text)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());

        log.info("Поиск вещей вернул {} результатов", result.size());
        return result;
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        log.info("Удаление вещи с ID={} пользователем с id={}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с ID={} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Пользователь с ID={} не владелец вещи с ID={}", userId, itemId);
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        itemRepository.delete(item);
        log.info("Вещь с ID={} успешно удалена", itemId);
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Добавление комментария пользователем с ID={} к вещи с ID={}", userId, itemId);

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            log.warn("Комментарий не может быть пустым");
            throw new ValidationException("Комментарий не может быть пустым");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID={} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с ID={} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        boolean hasBooking = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(userId, itemId,
                LocalDateTime.now());

        if (!hasBooking) {
            log.warn("Пользователь с ID={} не завершил бронирование вещи с ID={}", userId, itemId);
            throw new ValidationException("Юзер не завершил бронирование вещи");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        log.info("Комментарий успешно добавлен: {}", saved);
        return commentMapper.toCommentDto(saved);
    }
}
