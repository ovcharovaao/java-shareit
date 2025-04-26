package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestMapper requestMapper;
    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private final Long userId = 1L;
    private ItemRequest request;
    private ItemRequestDto requestDto;
    private User user;
    private Item item;
    private ItemShortDto itemShortDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@user.com");

        request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Request description");
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());

        requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("Request description");
        requestDto.setRequestorId(userId);
        requestDto.setCreated(request.getCreated());

        item = new Item();
        item.setId(1L);
        item.setName("Item name");
        item.setDescription("Item description");

        itemShortDto = new ItemShortDto();
        itemShortDto.setId(1L);
        itemShortDto.setName("Item name");
    }

    @Test
    void addRequest_ValidData_ReturnsItemRequestDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(requestMapper.toDto(any(ItemRequest.class))).thenReturn(requestDto);

        ItemRequestDto result = itemRequestService.addRequest(userId, "Request description");

        assertNotNull(result);
        assertEquals("Request description", result.getDescription());
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void addRequest_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.addRequest(userId,
                "Request description"));
    }

    @Test
    void getOwnRequests_ReturnsListOfItemRequestDtos() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(userId)).thenReturn(List.of(request));
        when(itemRepository.findByRequest_Id(anyLong())).thenReturn(List.of(item));
        when(itemMapper.toItemShortDto(any(Item.class))).thenReturn(itemShortDto);
        when(requestMapper.toDtoWithItems(any(ItemRequest.class), anyList())).thenReturn(requestDto);

        List<ItemRequestDto> result = itemRequestService.getOwnRequests(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(requestRepository).findByRequestorIdOrderByCreatedDesc(userId);
    }

    @Test
    void getOwnRequests_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getOwnRequests(userId));
    }

    @Test
    void getOthersRequests_ReturnsListOfItemRequestDtos() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.findAllExcludingUser(eq(userId), any()))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequest_Id(anyLong())).thenReturn(List.of(item));
        when(itemMapper.toItemShortDto(any(Item.class))).thenReturn(itemShortDto);
        when(requestMapper.toDtoWithItems(any(ItemRequest.class), anyList())).thenReturn(requestDto);

        List<ItemRequestDto> result = itemRequestService.getOthersRequests(userId, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(requestRepository).findAllExcludingUser(eq(userId), any());
    }

    @Test
    void getOthersRequests_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getOthersRequests(userId, 0, 10));
    }

    @Test
    void getRequestById_ReturnsItemRequestDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(itemRepository.findByRequest_Id(anyLong())).thenReturn(List.of(item));
        when(itemMapper.toItemShortDto(any(Item.class))).thenReturn(itemShortDto);
        when(requestMapper.toDtoWithItems(any(ItemRequest.class), anyList())).thenReturn(requestDto);

        ItemRequestDto result = itemRequestService.getRequestById(userId, request.getId());

        assertNotNull(result);
        assertEquals(requestDto.getId(), result.getId());
        verify(requestRepository).findById(request.getId());
    }

    @Test
    void getRequestById_RequestNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(userId, request.getId()));
    }

    @Test
    void getRequestById_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(userId, request.getId()));
    }

    @Test
    void addRequest_NullDescription_ReturnsItemRequestDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestMapper.toDto(any(ItemRequest.class))).thenAnswer(invocation -> {
            ItemRequest savedRequest = invocation.getArgument(0);
            ItemRequestDto dto = new ItemRequestDto();
            dto.setId(savedRequest.getId());
            dto.setDescription(savedRequest.getDescription());
            dto.setRequestorId(savedRequest.getRequestor().getId());
            dto.setCreated(savedRequest.getCreated());
            return dto;
        });

        ItemRequestDto result = itemRequestService.addRequest(userId, null);

        assertNotNull(result);
        assertNull(result.getDescription());
        verify(requestRepository).save(any(ItemRequest.class));
    }
}
