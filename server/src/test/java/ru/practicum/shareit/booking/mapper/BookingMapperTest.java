package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingMapperTest {
    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("Маппинг Booking в BookingDto должен работать корректно")
    void toBookingDto_shouldMapCorrectly() {
        User booker = new User(1L, "BookerName", "booker@example.com");
        Item item = new Item(1L, "ItemName", "ItemDescription", true, booker, null);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);

        BookingDto bookingDto = bookingMapper.toBookingDto(booking);

        assertThat(bookingDto).isNotNull();
        assertThat(bookingDto.getId()).isEqualTo(booking.getId());
        assertThat(bookingDto.getBooker()).isEqualTo(userMapper.toUserDto(booking.getBooker()));
        assertThat(bookingDto.getItem().getId()).isEqualTo(item.getId());
        assertThat(bookingDto.getItem().getName()).isEqualTo(item.getName());
    }
}
