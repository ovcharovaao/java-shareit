package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemShortDtoTest {

    @Test
    @DisplayName("Геттеры и сеттеры работают корректно")
    void gettersAndSetters_shouldWorkCorrectly() {
        ItemShortDto itemShortDto = new ItemShortDto();

        itemShortDto.setId(1L);
        itemShortDto.setName("Дрель");

        assertThat(itemShortDto.getId()).isEqualTo(1L);
        assertThat(itemShortDto.getName()).isEqualTo("Дрель");
    }

    @Test
    @DisplayName("Конструктор с параметрами работает корректно")
    void constructor_withParameters_shouldWorkCorrectly() {
        ItemShortDto itemShortDto = new ItemShortDto(1L, "Дрель");

        assertThat(itemShortDto.getId()).isEqualTo(1L);
        assertThat(itemShortDto.getName()).isEqualTo("Дрель");
    }

    @Test
    @DisplayName("equals и hashCode работают корректно")
    void equalsAndHashCode_shouldWorkCorrectly() {
        ItemShortDto item1 = new ItemShortDto(1L, "Дрель");
        ItemShortDto item2 = new ItemShortDto(1L, "Дрель");

        assertThat(item1).isEqualTo(item2);
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }

    @Test
    @DisplayName("Builder работает корректно")
    void builder_shouldWorkCorrectly() {
        ItemShortDto itemShortDto = ItemShortDto.builder()
                .id(1L)
                .name("Дрель")
                .build();

        assertThat(itemShortDto.getId()).isEqualTo(1L);
        assertThat(itemShortDto.getName()).isEqualTo("Дрель");
    }
}
