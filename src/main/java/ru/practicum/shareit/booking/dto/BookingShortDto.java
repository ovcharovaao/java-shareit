package ru.practicum.shareit.booking.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingShortDto {
    private Long id;
    private Long bookerId;
}
