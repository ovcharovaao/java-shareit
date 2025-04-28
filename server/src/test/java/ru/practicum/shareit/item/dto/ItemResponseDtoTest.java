package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemResponseDtoTest {

    @Test
    @DisplayName("Геттеры и сеттеры работают корректно")
    void gettersAndSetters_shouldWorkCorrectly() {
        ItemResponseDto itemResponseDto = new ItemResponseDto();

        BookingShortDto lastBooking = new BookingShortDto(1L, 2L);  // bookerId вместо строки
        BookingShortDto nextBooking = new BookingShortDto(2L, 3L);  // bookerId вместо строки

        itemResponseDto.setId(1L);
        itemResponseDto.setName("Дрель");
        itemResponseDto.setDescription("Мощная дрель");
        itemResponseDto.setAvailable(true);
        itemResponseDto.setRequestId(3L);
        itemResponseDto.setLastBooking(lastBooking);
        itemResponseDto.setNextBooking(nextBooking);
        itemResponseDto.setComments(List.of(new CommentDto(1L, "Комментарий", "Автор", null)));

        assertThat(itemResponseDto.getId()).isEqualTo(1L);
        assertThat(itemResponseDto.getName()).isEqualTo("Дрель");
        assertThat(itemResponseDto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(itemResponseDto.getAvailable()).isTrue();
        assertThat(itemResponseDto.getRequestId()).isEqualTo(3L);
        assertThat(itemResponseDto.getLastBooking()).isEqualTo(lastBooking);
        assertThat(itemResponseDto.getNextBooking()).isEqualTo(nextBooking);
        assertThat(itemResponseDto.getComments()).isNotEmpty();
        assertThat(itemResponseDto.getComments().get(0).getText()).isEqualTo("Комментарий");
    }

    @Test
    @DisplayName("equals и hashCode работают корректно")
    void equalsAndHashCode_shouldWorkCorrectly() {
        BookingShortDto lastBooking = new BookingShortDto(1L, 2L);  // bookerId вместо строки
        BookingShortDto nextBooking = new BookingShortDto(2L, 3L);  // bookerId вместо строки

        ItemResponseDto item1 = new ItemResponseDto(1L, "Дрель", "Мощная дрель", true, 3L, lastBooking, nextBooking, List.of(new CommentDto(1L, "Комментарий", "Автор", null)));
        ItemResponseDto item2 = new ItemResponseDto(1L, "Дрель", "Мощная дрель", true, 3L, lastBooking, nextBooking, List.of(new CommentDto(1L, "Комментарий", "Автор", null)));

        assertThat(item1).isEqualTo(item2);
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }

    @Test
    @DisplayName("Builder работает корректно")
    void builder_shouldWorkCorrectly() {
        BookingShortDto lastBooking = new BookingShortDto(1L, 2L);  // bookerId вместо строки
        BookingShortDto nextBooking = new BookingShortDto(2L, 3L);  // bookerId вместо строки

        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .requestId(3L)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(new CommentDto(1L, "Комментарий", "Автор", null)))
                .build();

        assertThat(itemResponseDto.getId()).isEqualTo(1L);
        assertThat(itemResponseDto.getName()).isEqualTo("Дрель");
        assertThat(itemResponseDto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(itemResponseDto.getAvailable()).isTrue();
        assertThat(itemResponseDto.getRequestId()).isEqualTo(3L);
        assertThat(itemResponseDto.getLastBooking()).isEqualTo(lastBooking);
        assertThat(itemResponseDto.getNextBooking()).isEqualTo(nextBooking);
        assertThat(itemResponseDto.getComments()).isNotEmpty();
    }
}
