package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;


@Data
public class RequestDto {
    private final Long id;
    private final String description;
    private final LocalDateTime created;
    private final List<ItemDto> items;
}
