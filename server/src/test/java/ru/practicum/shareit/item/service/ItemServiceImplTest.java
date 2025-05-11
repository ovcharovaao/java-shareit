package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@email.com");

        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
    }

    @Test
    @DisplayName("Создание вещи с валидными данными")
    void createItem_ValidData_ShouldReturnItemDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(any(ItemDto.class))).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.createItem(1L, itemDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Создание вещи с невалидным пользователем должно выбросить исключение")
    void createItem_InvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(999L, itemDto));
    }

    @Test
    @DisplayName("Обновление вещи с валидными данными")
    void updateItem_ValidUpdate_ShouldReturnUpdatedItem() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Item");
        updateDto.setDescription("New Description");
        updateDto.setAvailable(false);

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Updated Item");
        updatedItem.setDescription("New Description");
        updatedItem.setAvailable(false);
        updatedItem.setOwner(owner);

        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(1L);
        expectedDto.setName("Updated Item");
        expectedDto.setDescription("New Description");
        expectedDto.setAvailable(false);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(expectedDto);

        ItemDto result = itemService.updateItem(1L, 1L, updateDto);

        assertEquals("Updated Item", result.getName());
        assertEquals("New Description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    @DisplayName("Обновление вещи не владельцем должно выбросить исключение")
    void updateItem_NotByOwner_ShouldThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Another Name");

        assertThrows(NotFoundException.class, () -> itemService.updateItem(999L, 1L, updateDto));
    }

    @Test
    @DisplayName("Поиск вещей с валидным запросом возвращает результаты")
    void searchItems_WithValidQuery_ShouldReturnResults() {
        when(itemRepository.search(anyString())).thenReturn(List.of(item));
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItems("test");

        assertEquals(1, result.size());
        verify(itemRepository).search("test");
    }

    @Test
    @DisplayName("Поиск вещей с пустым запросом возвращает пустой список")
    void searchItems_WithEmptyQuery_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("   ");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Удаление вещи владельцем происходит успешно")
    void deleteItem_ByOwner_ShouldDeleteSuccessfully() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        itemService.deleteItem(1L, 1L);

        verify(itemRepository).delete(item);
    }

    @Test
    @DisplayName("Добавление комментария без бронирования должно выбросить исключение")
    void addComment_WithoutBooking_ShouldThrowValidationException() {
        CommentDto request = new CommentDto();
        request.setText("Great item");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.addComment(1L, 1L, request));
    }

    @Test
    @DisplayName("Добавление комментария с бронированием проходит успешно")
    void addComment_WithBooking_ShouldAddCommentSuccessfully() {
        CommentDto request = CommentDto.builder()
                .text("Awesome item")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Awesome item")
                .item(item)
                .author(owner)
                .build();

        CommentDto savedCommentDto = CommentDto.builder()
                .id(1L)
                .text("Awesome item")
                .authorName("Owner")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(savedCommentDto);

        CommentDto result = itemService.addComment(1L, 1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Awesome item", result.getText());
        assertEquals("Owner", result.getAuthorName());
    }

    @Test
    @DisplayName("Создание вещи с пустым названием должно выбросить исключение")
    void createItem_EmptyName_ShouldThrowValidationException() {
        ItemDto invalidDto = new ItemDto();
        invalidDto.setName(" ");
        invalidDto.setDescription("Valid");
        invalidDto.setAvailable(true);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class, () -> itemService.createItem(1L, invalidDto));
    }

    @Test
    @DisplayName("Обновление вещи не владельцем должно выбросить NotFoundException")
    void updateItem_NotOwner_ShouldThrowNotFoundException() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Name");

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(999L, 1L, updateDto));
    }

    @Test
    @DisplayName("Получение вещи с некорректным ID пользователя должно выбросить ValidationException")
    void getItem_InvalidUserId_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> itemService.getItem(0L, 1L));
    }

    @Test
    @DisplayName("Получение вещи с некорректным ID вещи должно выбросить ValidationException")
    void getItem_InvalidItemId_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> itemService.getItem(1L, 0L));
    }

    @Test
    @DisplayName("Удаление вещи не владельцем должно выбросить NotFoundException")
    void deleteItem_NotOwner_ShouldThrowNotFoundException() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setName("Another");
        anotherUser.setEmail("another@email.com");

        Item itemFromDb = new Item();
        itemFromDb.setId(1L);
        itemFromDb.setOwner(anotherUser);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(itemFromDb));

        assertThrows(NotFoundException.class, () -> itemService.deleteItem(1L, 1L));
    }

    @Test
    @DisplayName("Создание комментария с пустым текстом должно выбросить ValidationException")
    void addComment_EmptyText_ShouldThrowValidationException() {
        CommentDto request = new CommentDto();
        request.setText("   ");

        assertThrows(ValidationException.class, () -> itemService.addComment(1L, 1L, request));
    }

    @Test
    @DisplayName("Создание вещи без указания доступности должно выбросить ValidationException")
    void createItem_NullAvailable_ShouldThrowValidationException() {
        ItemDto invalidDto = new ItemDto();
        invalidDto.setName("Valid Name");
        invalidDto.setDescription("Valid Description");
        invalidDto.setAvailable(null);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class, () -> itemService.createItem(1L, invalidDto));
    }

    @Test
    @DisplayName("Создание вещи с пустым описанием должно выбросить ValidationException")
    void createItem_EmptyDescription_ShouldThrowValidationException() {
        ItemDto invalidDto = new ItemDto();
        invalidDto.setName("Valid Name");
        invalidDto.setDescription(" ");
        invalidDto.setAvailable(true);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class, () -> itemService.createItem(1L, invalidDto));
    }

    @Test
    @DisplayName("Получение всех вещей пользователя возвращает список вещей")
    void getItemsByUser_ShouldReturnListOfItems() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner_Id(anyLong(), any(Sort.class))).thenReturn(List.of(item));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(anyLong(), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findPastOwnerBookings(anyLong(), anyLong(), any())).thenReturn(List.of());
        when(bookingRepository.findFutureOwnerBookings(anyLong(), anyLong(), any())).thenReturn(List.of());

        List<ItemResponseDto> result = itemService.getItemsByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
    }

    @Test
    void deleteItem_ShouldDeleteItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).delete(any(Item.class));

        itemService.deleteItem(1L, 1L);

        verify(itemRepository, times(1)).delete(any(Item.class));
    }

    @Test
    void deleteItem_ShouldThrowNotFoundException_WhenItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.deleteItem(1L, 1L));
    }

    @Test
    void searchItems_ShouldReturnItemDtos() {
        when(itemRepository.search(anyString()))
                .thenReturn(List.of(item));
        when(itemMapper.toItemDto(any(Item.class)))
                .thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItems("Item");

        assertEquals(1, result.size());
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextIsEmpty() {
        List<ItemDto> result = itemService.searchItems("");

        assertTrue(result.isEmpty());
    }

    @Test
    void getItem_ShouldThrowNotFoundException_WhenItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItem(1L, 1L));
    }

    @Test
    void createItem_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(1L, itemDto));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItemDto() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.updateItem(1L, 1L, itemDto);

        assertEquals(itemDto, result);
    }

    @Test
    void updateItem_ShouldThrowNotFoundException_WhenItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, 1L, itemDto));
    }
}
