package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
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
    void updateItem_NotByOwner_ShouldThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Another Name");

        assertThrows(NotFoundException.class, () -> itemService.updateItem(999L, 1L, updateDto));
    }

    @Test
    void searchItems_WithValidQuery_ShouldReturnResults() {
        when(itemRepository.search(anyString())).thenReturn(List.of(item));
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItems("test");

        assertEquals(1, result.size());
        verify(itemRepository).search("test");
    }

    @Test
    void searchItems_WithEmptyQuery_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("   ");

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteItem_ByOwner_ShouldDeleteSuccessfully() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        itemService.deleteItem(1L, 1L);

        verify(itemRepository).delete(item);
    }

    @Test
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
}
