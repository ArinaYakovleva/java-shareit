package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

public abstract class ItemDTOMapper {
    public static ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }

        return new ItemDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable());
    }

    public static Item fromItemDto(ItemDto itemDto, Long ownerId) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                ownerId,
                itemDto.getAvailable(),
                null
        );
    }
}
