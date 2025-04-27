package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemDtoTest {

    @Test
    @DisplayName("Геттеры и сеттеры работают корректно")
    void gettersAndSetters_shouldWorkCorrectly() {
        ItemDto itemDto = new ItemDto();

        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель для ремонта");
        itemDto.setAvailable(true);
        itemDto.setRequestId(2L);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Дрель");
        assertThat(itemDto.getDescription()).isEqualTo("Мощная дрель для ремонта");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("equals и hashCode работают правильно")
    void equalsAndHashCode_shouldWorkCorrectly() {
        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Стул");

        ItemDto item2 = new ItemDto();
        item2.setId(1L);
        item2.setName("Стул");

        assertThat(item1).isEqualTo(item2);
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }
}
