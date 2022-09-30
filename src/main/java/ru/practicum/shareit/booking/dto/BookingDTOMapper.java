package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;

public abstract class BookingDTOMapper {
    public BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getItemId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus()
        );
    }
}
