package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(Long userId, String description);

    List<ItemRequestDto> getOwnRequests(Long userId);

    List<ItemRequestDto> getOthersRequests(Long userId, int from, int size);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}
