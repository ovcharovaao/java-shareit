package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /requests - создать запрос")
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        ItemRequestDto createdRequest = new ItemRequestDto();
        createdRequest.setId(1L);
        createdRequest.setDescription("Need a drill");

        when(itemRequestService.addRequest(1L, "Need a drill")).thenReturn(createdRequest);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(Map.of("description", "Need a drill")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdRequest.getId()))
                .andExpect(jsonPath("$.description").value(createdRequest.getDescription()));
    }

    @Test
    @DisplayName("GET /requests - получить свои запросы")
    void getOwnRequests_shouldReturnList() throws Exception {
        when(itemRequestService.getOwnRequests(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /requests/all - получить все запросы")
    void getAllRequests_shouldReturnList() throws Exception {
        when(itemRequestService.getOthersRequests(1L, 0, 10)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /requests/{requestId} - получить запрос по ID")
    void getRequestById_shouldReturnRequest() throws Exception {
        ItemRequestDto request = new ItemRequestDto();
        request.setId(2L);
        request.setDescription("Need a hammer");

        when(itemRequestService.getRequestById(1L, 2L)).thenReturn(request);

        mockMvc.perform(get("/requests/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request.getId()))
                .andExpect(jsonPath("$.description").value(request.getDescription()));
    }

    @Test
    @DisplayName("GET /requests/all - валидация параметров пагинации")
    void getAllRequests_shouldReturnBadRequestIfInvalidParams() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")  // некорректный from
                        .param("size", "0"))  // некорректный size
                .andExpect(status().isBadRequest());
    }
}
