package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
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

    private final User owner = User.builder()
            .id(1L)
            .name("Owner")
            .email("owner@email.com")
            .build();

    private final Item item = Item.builder()
            .id(1L)
            .name("Item")
            .description("Description")
            .available(true)
            .owner(owner)
            .build();

    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("Item")
            .description("Description")
            .available(true)
            .build();

    @Test
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
    void createItem_InvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(999L, itemDto));
    }

    @Test
    void updateItem_ValidUpdate_ShouldReturnUpdatedItem() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Item")
                .description("New Description")
                .available(false)
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name("Updated Item")
                .description("New Description")
                .available(false)
                .owner(owner)
                .build();

        ItemDto expectedDto = ItemDto.builder()
                .id(1L)
                .name("Updated Item")
                .description("New Description")
                .available(false)
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(expectedDto);

        ItemDto result = itemService.updateItem(1L, 1L, updateDto);

        assertEquals("Updated Item", result.getName());
        assertEquals("New Description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void searchItems_WithValidQuery_ShouldReturnResults() {
        when(itemRepository.search(anyString()))
                .thenReturn(List.of(item));
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItems("test");

        assertEquals(1, result.size());
        verify(itemRepository).search("test");
    }

    @Test
    void deleteItem_ByOwner_ShouldDeleteSuccessfully() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        itemService.deleteItem(1L, 1L);

        verify(itemRepository).delete(item);
    }

    @Test
    void addComment_WithoutBooking_ShouldThrowValidationException() {
        CommentDto request = CommentDto.builder()
                .text("Great item!")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(false);

        assertThrows(ValidationException.class,
                () -> itemService.addComment(1L, 1L, request));
    }

    @Test
    void updateItem_NotByOwner_ShouldThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ItemDto updateDto = ItemDto.builder().name("New Name").build();

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(999L, 1L, updateDto));
    }

    @Test
    void searchItems_WithEmptyQuery_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("   ");

        assertTrue(result.isEmpty());
    }
}
