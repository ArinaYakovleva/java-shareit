package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class BookingDto {
    private final Long id;
    private final Long itemId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BookingStatus status;
}
