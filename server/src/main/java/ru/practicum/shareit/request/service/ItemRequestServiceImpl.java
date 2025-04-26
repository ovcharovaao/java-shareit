package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ItemRequestMapper requestMapper;

    @Override
    public ItemRequestDto addRequest(Long userId, String description) {
        log.info("Создание запроса вещи пользователем ID={}, описание={}", userId, description);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID={} не найден при создании запроса", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        log.info("Запрос ID={} успешно создан", savedRequest.getId());

        return requestMapper.toDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        log.info("Получение собственных запросов пользователя ID={}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID={} не найден при получении своих запросов", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        log.info("Найдено {} собственных запросов для пользователя ID={}", requests.size(), userId);

        return requests.stream()
                .map(this::toDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getOthersRequests(Long userId, int from, int size) {
        log.info("Получение запросов других пользователей. Запрашивающий ID={}, from={}, size={}", userId, from, size);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID={} не найден при получении чужих запросов", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = requestRepository.findAllExcludingUser(userId, pageable);
        log.info("Найдено {} запросов других пользователей для пользователя ID={}", requests.size(), userId);

        return requests.stream()
                .map(this::toDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.info("Получение запроса ID={} пользователем ID={}", requestId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID={} не найден при получении запроса ID={}", userId, requestId);
                    return new NotFoundException("Пользователь не найден");
                });

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Запрос с ID={} не найден", requestId);
                    return new NotFoundException("Запрос не найден");
                });

        List<ItemShortDto> itemDtos = itemRepository.findByRequest_Id(requestId).stream()
                .map(itemMapper::toItemShortDto)
                .collect(Collectors.toList());

        log.info("Запрос ID={} успешно получен", requestId);
        return requestMapper.toDtoWithItems(request, itemDtos);
    }

    private ItemRequestDto toDtoWithItems(ItemRequest request) {
        List<ItemShortDto> items = itemRepository.findByRequest_Id(request.getId()).stream()
                .map(itemMapper::toItemShortDto)
                .collect(Collectors.toList());
        return requestMapper.toDtoWithItems(request, items);
    }
}
