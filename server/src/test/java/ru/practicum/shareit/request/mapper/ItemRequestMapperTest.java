package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemRequestMapperTest {
    @Autowired
    private ItemRequestMapper itemRequestMapper;

    @Test
    @DisplayName("Маппинг ItemRequest в ItemRequestDto должен работать корректно без предметов")
    void toDto_shouldMapCorrectly() {
        User requestor = new User(1L, "RequestorName", "requestor@example.com");

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Описание запроса")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto itemRequestDto = itemRequestMapper.toDto(itemRequest);

        assertThat(itemRequestDto).isNotNull();
        assertThat(itemRequestDto.getId()).isEqualTo(itemRequest.getId());
        assertThat(itemRequestDto.getDescription()).isEqualTo(itemRequest.getDescription());
        assertThat(itemRequestDto.getRequestorId()).isEqualTo(itemRequest.getRequestor().getId());
        assertThat(itemRequestDto.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Маппинг ItemRequest в ItemRequestDto с предметами должен работать корректно")
    void toDtoWithItems_shouldMapCorrectly() {
        User requestor = new User(1L, "RequestorName", "requestor@example.com");
        ItemShortDto itemShortDto = new ItemShortDto(1L, "ItemName");

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Описание запроса")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto itemRequestDto = itemRequestMapper.toDtoWithItems(itemRequest, Collections.singletonList(itemShortDto));

        assertThat(itemRequestDto).isNotNull();
        assertThat(itemRequestDto.getId()).isEqualTo(itemRequest.getId());
        assertThat(itemRequestDto.getDescription()).isEqualTo(itemRequest.getDescription());
        assertThat(itemRequestDto.getRequestorId()).isEqualTo(itemRequest.getRequestor().getId());
        assertThat(itemRequestDto.getItems()).hasSize(1);
        assertThat(itemRequestDto.getItems().get(0).getId()).isEqualTo(itemShortDto.getId());
        assertThat(itemRequestDto.getItems().get(0).getName()).isEqualTo(itemShortDto.getName());
    }
}
