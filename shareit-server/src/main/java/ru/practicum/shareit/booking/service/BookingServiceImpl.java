package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper mapper;

    @Override
    public BookingDto createBooking(Long userId, BookingRequestDto dto) {
        log.info("Создание бронирования пользователем ID: {}", userId);

        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new ValidationException("Дата начала и окончания не могут быть пустыми");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }
        if (dto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + dto.getItemId() + " не найдена"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Нельзя бронировать собственную вещь");
        }

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование создано с ID: {}", savedBooking.getId());

        return mapper.toBookingDto(savedBooking);
    }

    @Override
    public BookingDto updateBooking(Long userId, Long bookingId, Boolean approved) {
        log.info("Обновление бронирования id: {} пользователем ID: {}. Статус: {}", bookingId, userId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Пользователь не является владельцем вещи");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Бронирование уже было обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Бронирование ID: {} обновлено. Новый статус: {}", bookingId, booking.getStatus());

        return mapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        log.info("Получение бронирования ID: {} пользователем ID: {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Доступ к бронированию запрещён");
        }

        return mapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state, int from, int size) {
        log.info("Получение бронирований пользователя ID: {}. Статус: {}, from: {}, size: {}",
                userId, state, from, size);

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
                throw new ValidationException("Неизвестный статус бронирования: " + state);
        }
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, String state) {
        log.info("Получение бронирований владельца ID: {}. Статус: {}", ownerId, state);

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + ownerId + " не найден"));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);

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
                throw new ValidationException("Неизвестный статус бронирования: " + state);
        }
    }
}
