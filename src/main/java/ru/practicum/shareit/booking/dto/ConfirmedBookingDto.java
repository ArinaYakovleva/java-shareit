package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
public class ConfirmedBookingDto {
    private final Long id;
    private final BookingStatus status;
    private final User booker;
    private final Item item;
    private final LocalDateTime start;
    private final LocalDateTime end;
}
