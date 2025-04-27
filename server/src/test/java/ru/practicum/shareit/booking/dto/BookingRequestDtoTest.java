package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRequestDtoTest {

    @Test
    @DisplayName("Создание объекта BookingRequestDto через конструктор должно работать корректно")
    void shouldCreateBookingRequestDto() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        BookingRequestDto requestDto = new BookingRequestDto(1L, start, end);

        assertThat(requestDto.getItemId()).isEqualTo(1L);
        assertThat(requestDto.getStart()).isEqualTo(start);
        assertThat(requestDto.getEnd()).isEqualTo(end);
    }
}
