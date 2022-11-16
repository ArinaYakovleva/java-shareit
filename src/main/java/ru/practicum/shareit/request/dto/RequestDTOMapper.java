package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemDTOMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RequestDTOMapper {
    public static RequestDto toRequestDto(Request request, List<Item> items) {
        return new RequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items.stream()
                        .map(ItemDTOMapper::toItemDto)
                        .collect(Collectors.toList())
        );
    }

    public static Request fromCreateRequestDto(CreateRequestDto requestDto, User user, List<Item> items, LocalDateTime created) {
        return new Request(
                requestDto.getId(),
                requestDto.getDescription(),
                user,
                created
        );
    }
}
