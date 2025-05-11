package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    @DisplayName("Создание новой вещи")
    void createItem_shouldReturnCreatedItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);

        Mockito.when(itemService.createItem(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    @DisplayName("Обновление вещи по ID")
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель обновленная");
        itemDto.setDescription("Обновленная мощная дрель");
        itemDto.setAvailable(false);

        Mockito.when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    @DisplayName("Получение вещи по ID")
    void getItem_shouldReturnItem() throws Exception {
        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Дрель");
        responseDto.setDescription("Мощная дрель");
        responseDto.setAvailable(true);

        Mockito.when(itemService.getItem(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.name").value(responseDto.getName()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.available").value(responseDto.getAvailable()));
    }

    @Test
    @DisplayName("Получение всех вещей пользователя")
    void getItemsByUser_shouldReturnList() throws Exception {
        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Дрель");

        Mockito.when(itemService.getItemsByUser(anyLong())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()))
                .andExpect(jsonPath("$[0].name").value(responseDto.getName()));
    }

    @Test
    @DisplayName("Поиск вещей по тексту")
    void searchItems_shouldReturnMatchingItems() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");

        Mockito.when(itemService.searchItems(any())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()));
    }

    @Test
    @DisplayName("Удаление вещи по ID")
    void deleteItem_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(itemService, Mockito.times(1)).deleteItem(1L, 1L);
    }

    @Test
    @DisplayName("Добавление комментария к вещи")
    void addComment_shouldReturnComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Отличная вещь!");

        Mockito.when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()));
    }

    @Test
    @DisplayName("Обновление вещи через PATCH без указания itemId в URL")
    void updateItemFromBody_ShouldReturnUpdatedItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Updated Name");

        Mockito.when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(patch("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }
}
