package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemMapperTest {
    @Autowired
    private ItemMapper mapper;

    @Test
    @DisplayName("toItemDto должен корректно преобразовывать Item в ItemDto")
    void toItemDto_shouldMapCorrectly() {
        Item item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();

        ItemDto itemDto = mapper.toItemDto(item);

        assertThat(itemDto).isNotNull();
        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Дрель");
        assertThat(itemDto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isNull();
    }

    @Test
    @DisplayName("toItem должен корректно преобразовывать ItemDto в Item")
    void toItem_shouldMapCorrectly() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Отвертка");
        itemDto.setDescription("Крестовая отвертка");
        itemDto.setAvailable(false);

        Item item = mapper.toItem(itemDto);

        assertThat(item).isNotNull();
        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Отвертка");
        assertThat(item.getDescription()).isEqualTo("Крестовая отвертка");
        assertThat(item.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("toItemShortDto должен корректно преобразовывать Item в ItemShortDto")
    void toItemShortDto_shouldMapCorrectly() {
        Item item = Item.builder()
                .id(10L)
                .name("Перфоратор")
                .build();

        ItemShortDto shortDto = mapper.toItemShortDto(item);

        assertThat(shortDto).isNotNull();
        assertThat(shortDto.getId()).isEqualTo(10L);
        assertThat(shortDto.getName()).isEqualTo("Перфоратор");
    }

    @Test
    @DisplayName("toItemDto, toItem и toItemShortDto должны корректно работать с null")
    void shouldHandleNullInputs() {
        assertThat(mapper.toItemDto(null)).isNull();
        assertThat(mapper.toItem(null)).isNull();
        assertThat(mapper.toItemShortDto(null)).isNull();
    }
}
