package ru.practicum.shareit.request.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateRequestDto {
    private final Long id;
    @NotNull
    @NotBlank
    private final String description;
}
