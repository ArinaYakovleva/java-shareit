package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long ownerId);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);

    ItemDto editItem(Long id, ItemDto item, Long ownerId);

    ItemBookingDto getItem(Long id, Long userId);

    List<ItemBookingDto> getAllItems(Long ownerId, Integer from, Integer size);

    List<ItemDto> searchItems(String searchStr, Integer from, Integer size);

    void deleteItem(Long id, Long ownerId);
}
