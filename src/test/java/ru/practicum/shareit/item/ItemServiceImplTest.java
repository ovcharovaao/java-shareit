package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemServiceImplTest {
    private ItemServiceImpl itemService;
    private UserDto userDto;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ItemMapper itemMapper;

    @BeforeEach
    void setUp() {
        UserServiceImpl userService = new UserServiceImpl(userMapper);
        itemService = new ItemServiceImpl(userService, itemMapper, userMapper);

        userDto = new UserDto(null, "User", "user@email.com");
        userDto = userService.createUser(userDto);
    }

    @Test
    void createItem() {
        ItemDto itemDto = new ItemDto(null, "Item1", "Description1", true, null);
        ItemDto createdItem = itemService.createItem(userDto.getId(), itemDto);

        assertNotNull(createdItem.getId());
        assertEquals("Item1", createdItem.getName());
        assertEquals("Description1", createdItem.getDescription());
        assertTrue(createdItem.getAvailable());
    }

    @Test
    void getItem() {
        ItemDto itemDto = new ItemDto(null, "Item1", "Description1", true, null);
        ItemDto createdItem = itemService.createItem(userDto.getId(), itemDto);
        ItemDto fetchedItem = itemService.getItem(userDto.getId(), createdItem.getId());

        assertEquals(createdItem.getId(), fetchedItem.getId());
        assertEquals(createdItem.getName(), fetchedItem.getName());
        assertEquals(createdItem.getDescription(), fetchedItem.getDescription());
    }

    @Test
    void updateItem() {
        ItemDto itemDto = new ItemDto(null, "Item1", "Description1", true, null);
        ItemDto createdItem = itemService.createItem(userDto.getId(), itemDto);
        ItemDto updateDto = new ItemDto(null, "UpdatedItem",
                "UpdatedDescription", false, null);

        ItemDto updatedItem = itemService.updateItem(userDto.getId(), createdItem.getId(), updateDto);

        assertEquals("UpdatedItem", updatedItem.getName());
        assertEquals("UpdatedDescription", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void searchItems() {
        itemService.createItem(userDto.getId(), new ItemDto(null, "Name1", "Description1",
                true, null));
        itemService.createItem(userDto.getId(), new ItemDto(null, "Name2", "Description2",
                true, null));
        List<ItemDto> results = itemService.searchItems("name1");

        assertEquals(1, results.size());
        assertEquals("Name1", results.get(0).getName());
    }

    @Test
    void createItemWithEmptyName() {
        ItemDto itemDto = new ItemDto(null, "", "Description", true, null);
        assertThrows(ValidationException.class, () -> itemService.createItem(userDto.getId(), itemDto));
    }

    @Test
    void getItemWhenItemNotExists() {
        assertThrows(NotFoundException.class, () -> itemService.getItem(userDto.getId(), 999L));
    }

    @Test
    void updateItemWhenItemNotExists() {
        ItemDto updateDto = new ItemDto(null, "UpdatedItem", "UpdatedDescription",
                false, null);
        assertThrows(NotFoundException.class, () -> itemService.updateItem(userDto.getId(), 999L, updateDto));
    }

    @Test
    void searchItemsWhenNoMatch() {
        itemService.createItem(userDto.getId(), new ItemDto(null, "Name",
                "Description", true, null));
        List<ItemDto> results = itemService.searchItems("Name1");

        assertTrue(results.isEmpty());
    }
}
