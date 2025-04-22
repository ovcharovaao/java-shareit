package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingMapper mapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final User user = new User(1L, "user", "user@mail.ru");
    private final User owner = new User(2L, "owner", "owner@mail.ru");
    private final Item item = new Item(1L, "item", "description", true, owner, null);
    private final Booking booking = new Booking(
            1L,
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusDays(1),
            item,
            user,
            BookingStatus.WAITING
    );

    private final BookingDto bookingDto = new BookingDto(
            1L,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            BookingStatus.WAITING,
            new UserDto(1L, "user", "user@mail.ru"),
            new ItemShortDto(1L, "item")
    );

    @Test
    void createBooking_ValidData_ReturnsBookingDto() {
        BookingRequestDto request = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(mapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.createBooking(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateBooking_ApproveBooking_ReturnsApprovedBooking() {
        Booking approvedBooking = new Booking(
                1L,
                booking.getStart(),
                booking.getEnd(),
                item,
                user,
                BookingStatus.APPROVED
        );

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(approvedBooking);
        when(mapper.toBookingDto(any())).thenReturn(
                new BookingDto(
                        1L,
                        approvedBooking.getStart(),
                        approvedBooking.getEnd(),
                        BookingStatus.APPROVED,
                        bookingDto.getBooker(),
                        bookingDto.getItem()
                )
        );

        BookingDto result = bookingService.updateBooking(2L, 1L, true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingRepository).save(approvedBooking);
    }
}