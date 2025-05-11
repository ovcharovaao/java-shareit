package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты для ItemRequestDto")
class ItemRequestDtoTest {

    @Test
    @DisplayName("Проверка корректности установки и получения полей ItemRequestDto")
    void testItemRequestDtoFields() {
        Long id = 1L;
        String description = "Description";
        Long requestorId = 2L;
        LocalDateTime created = LocalDateTime.now();
        ItemShortDto item = new ItemShortDto();
        item.setId(1L);
        item.setName("Name");

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(id);
        dto.setDescription(description);
        dto.setRequestorId(requestorId);
        dto.setCreated(created);
        dto.setItems(List.of(item));

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getRequestorId()).isEqualTo(requestorId);
        assertThat(dto.getCreated()).isEqualTo(created);
        assertThat(dto.getItems()).containsExactly(item);
    }
}
