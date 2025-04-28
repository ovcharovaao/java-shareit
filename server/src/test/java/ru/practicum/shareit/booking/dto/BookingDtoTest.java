package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingDtoTest {

    @Test
    @DisplayName("Построение объекта BookingDto через builder должно работать корректно")
    void shouldCreateBookingDtoWithBuilder() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        UserDto userDto = new UserDto(1L, "User", "user@example.com");
        ItemShortDto itemShortDto = new ItemShortDto(1L, "Item1");

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(BookingStatus.APPROVED)
                .booker(userDto)
                .item(itemShortDto)
                .build();

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(start);
        assertThat(bookingDto.getEnd()).isEqualTo(end);
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookingDto.getBooker()).isEqualTo(userDto);
        assertThat(bookingDto.getItem()).isEqualTo(itemShortDto);
    }
}
