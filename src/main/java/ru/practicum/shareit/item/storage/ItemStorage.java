package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.BaseItemService;
import ru.practicum.shareit.item.model.Item;

public interface ItemStorage extends BaseItemService {
    Item deleteItem(Long id, Long ownerId);
}
