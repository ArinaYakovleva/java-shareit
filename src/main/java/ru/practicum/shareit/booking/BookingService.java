package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ConfirmedBookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.List;

public interface BookingService {
    CreateBookingDto addBooking(CreateBookingDto bookingDto, Long bookerId);

    ConfirmedBookingDto confirmBooking(Long bookingId, Long userId, Boolean isApproved);

    BookingDto getBooking(Long bookingId, Long userId);

    List<BookingDto> getAllBookings(String state, Long userId, boolean isByOwner);
}
