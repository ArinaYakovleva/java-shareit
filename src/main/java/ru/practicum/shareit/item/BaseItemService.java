package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface BaseItemService {
    ItemDto addItem(ItemDto itemDto, Long ownerId);

    ItemDto editItem(Long id, ItemDto item, Long ownerId);

    ItemDto getItem(Long id);

    List<ItemDto> getAllItems(Long ownerId);

    List<ItemDto> searchItems(String searchStr);

    void deleteItem(Long id, Long ownerId);
}
