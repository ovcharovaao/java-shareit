package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Создание бронирования должно возвращать BookingDto со статусом WAITING")
    void createBooking_shouldReturnBookingDto() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        Mockito.when(bookingService.createBooking(anyLong(), any()))
                .thenReturn(bookingDto);

        BookingRequestDto requestDto = new BookingRequestDto(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    @DisplayName("Подтверждение бронирования должно возвращать обновлённый BookingDto со статусом APPROVED")
    void approveBooking_shouldReturnUpdatedBookingDto() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        Mockito.when(bookingService.updateBooking(anyLong(), anyLong(), any()))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Получение бронирования по ID должно возвращать правильный BookingDto")
    void getBookingById_shouldReturnBookingDto() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .build();

        Mockito.when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Получение списка бронирований владельца должно возвращать список BookingDto")
    void listByOwner_shouldReturnListOfBookings() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .build();

        Mockito.when(bookingService.getOwnerBookings(anyLong(), any()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("Получение списка бронирований пользователя должно возвращать список BookingDto")
    void findByUserId_shouldReturnListOfBookings() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .build();

        Mockito.when(bookingService.getUserBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}
