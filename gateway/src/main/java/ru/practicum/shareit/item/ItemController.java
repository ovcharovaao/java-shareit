package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemClient client;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        return client.createItem(userId, itemDto);
    }

    @PatchMapping
    public ResponseEntity<Object> updateItemFromBody(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestBody ItemDto itemDto) {
        if (itemDto.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return client.updateItem(userId, itemDto.getId(), itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId, @RequestBody ItemDto itemDto) {
        return client.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return client.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.getItemsByUser(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam(value = "text", required = false) String text) {
        if (text == null || text.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return client.searchItems(text);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId) {
        return client.deleteItem(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId,
                                             @Valid @RequestBody CommentDto commentDto) {
        return client.addComment(userId, itemId, commentDto);
    }
}
