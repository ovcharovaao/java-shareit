package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookingShortDtoTest {

    @Test
    @DisplayName("Создание объекта BookingShortDto через builder должно работать корректно")
    void shouldCreateBookingShortDtoWithBuilder() {
        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .id(1L)
                .bookerId(2L)
                .build();

        assertThat(bookingShortDto.getId()).isEqualTo(1L);
        assertThat(bookingShortDto.getBookerId()).isEqualTo(2L);
    }
}
