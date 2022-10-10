package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemServiceImpl service;

    @Autowired
    public ItemController(ItemServiceImpl service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemBookingDto> getAllItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return service.getAllItems(ownerId);
    }

    @GetMapping("/{id}")
    public ItemBookingDto getItem(@PathVariable Long id, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return service.getItem(id, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(required = false) String text) {
        return service.searchItems(text);
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long ownerId, @Valid @RequestBody ItemDto item) {
        return service.addItem(item, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody CommentDto comment) {
        return service.addComment(itemId, userId, comment);
    }

    @PatchMapping("/{id}")
    public ItemDto editItem(@PathVariable Long id, @RequestHeader("X-Sharer-User-Id") Long ownerId,
                            @RequestBody ItemDto item) {
        return service.editItem(id, item, ownerId);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        service.deleteItem(id, ownerId);
    }
}
