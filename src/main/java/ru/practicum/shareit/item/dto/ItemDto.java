package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ItemDto {
    private final Long id;
    @NotNull(message = "Название не должно равняться null")
    @NotBlank(message = "Название не должно быть пустым")
    private final String name;

    @NotNull(message = "Описание не должно равняться null")
    @NotBlank(message = "Описание не должно быть пустым null")
    private final String description;

    @NotNull(message = "Доступность не должна равняться null")
    private final Boolean available;

    private final Long requestId;
}
