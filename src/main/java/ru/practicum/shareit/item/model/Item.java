package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class Item {
    private final Long id;
    private final String name;
    private final String description;
    private final Long ownerId;
    private final Boolean available;
    private final ItemRequest request;
}
