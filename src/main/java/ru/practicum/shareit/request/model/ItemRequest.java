package ru.practicum.shareit.request.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ItemRequest {
    private final Long id;
    private final String description;
    private final Long requestorId;
    private final LocalDate creationDate;
}
