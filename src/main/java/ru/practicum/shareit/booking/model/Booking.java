package ru.practicum.shareit.booking.model;

import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDate;

@Data
public class Booking {
    private final Long id;
    private final Long itemId;
    private final Long bookerId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BookingStatus status;
}
