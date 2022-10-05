package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import java.util.List;


@Data
public class ItemBookingDto {
    private final Long id;
    private final String name;
    private final String description;
    private final Boolean available;
    private final BookingItemDto lastBooking;
    private final BookingItemDto nextBooking;
    private final List<CommentDto> comments;
}
