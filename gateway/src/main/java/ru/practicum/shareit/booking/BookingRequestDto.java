package ru.practicum.shareit.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequestDto {
    @NotNull(message = "ID вещи не может быть пустым")
    private Long itemId;

    @NotNull(message = "Дата начала бронирования должна быть указана")
    @Future(message = "Дата и время начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "Дата конца бронирования должна быть указана")
    @Future(message = "Дата и время конца бронирования не может быть в прошлом")
    private LocalDateTime end;
}
