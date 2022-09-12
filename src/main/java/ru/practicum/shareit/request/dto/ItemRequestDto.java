package ru.practicum.shareit.request.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequestDto {
    private final Long id;
    private final String description;
    private final LocalDate creationDate;
}
