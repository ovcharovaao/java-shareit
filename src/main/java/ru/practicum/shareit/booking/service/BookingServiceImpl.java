package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.booking.mapper.BookingMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper mapper;

    @Override
    public BookingDto createBooking(Long userId, BookingRequestDto dto) {
        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new ValidationException("Start and end cannot be null");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("End must be after start");
        }
        if (dto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Start cannot be in the past");
        }

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!item.getAvailable()) {
            throw new ValidationException("Item not available");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Cannot book your own item");
        }

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);

        return mapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto updateBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Бронирование уже было обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return mapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Access denied");
        }

        return mapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByBookerId(userId, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, now, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findByBookerIdAndEndBefore(userId, now, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findByBookerIdAndStartAfter(userId, now, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, String state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sort); // Change 0 and Integer.MAX_VALUE to your pagination if needed

        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByItemOwner_Id(ownerId, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(ownerId, now, now, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findByItemOwnerIdAndEndBefore(ownerId, now, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findByItemOwnerIdAndStartAfter(ownerId, now, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, pageable)
                        .stream().map(mapper::toBookingDto).collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }
}
