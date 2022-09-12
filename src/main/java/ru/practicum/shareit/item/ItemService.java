package ru.practicum.shareit.item;

public interface ItemService extends BaseItemService {
    void deleteItem(Long id, Long ownerId);
}
