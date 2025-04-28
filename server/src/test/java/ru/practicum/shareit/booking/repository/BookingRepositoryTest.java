package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("Поиск бронирований пользователя по статусу с пагинацией")
    void findByBookerIdAndStatus_shouldReturnBookingsWhenStatusMatches() {
        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        booker = userRepository.save(booker);

        Item item = new Item();
        item.setName("Item1");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(booker);
        item = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> result = bookingRepository.findByBookerIdAndStatus(booker.getId(), BookingStatus.APPROVED, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("Поиск будущих бронирований владельца предмета с пагинацией")
    void findByItemOwnerIdAndStartAfter_shouldReturnFutureBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        Item item = new Item();
        item.setName("Item1");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        booker = userRepository.save(booker);

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> result = bookingRepository.findByItemOwnerIdAndStartAfter(owner.getId(), LocalDateTime.now(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStart()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Проверка существования бронирования с учетом времени окончания")
    void existsByBookerIdAndItemIdAndEndBefore_shouldReturnTrueWhenBookingExists() {
        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        booker = userRepository.save(booker);

        Item item = new Item();
        item.setName("Item1");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(booker);
        item = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        boolean exists = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(booker.getId(), item.getId(), LocalDateTime.now());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Поиск прошлых бронирований владельца предмета")
    void findPastOwnerBookings_shouldReturnPastBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        Item item = new Item();
        item.setName("Item1");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        booker = userRepository.save(booker);

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        List<Booking> result = bookingRepository.findPastOwnerBookings(item.getId(), owner.getId(), LocalDateTime.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.APPROVED);
    }
}
