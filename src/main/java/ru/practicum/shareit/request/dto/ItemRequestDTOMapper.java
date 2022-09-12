package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.request.model.ItemRequest;

public abstract class ItemRequestDTOMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(itemRequest.getId(), itemRequest.getDescription(), itemRequest.getCreationDate());
    }
}
