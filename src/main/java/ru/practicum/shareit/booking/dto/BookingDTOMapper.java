package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public final class BookingDTOMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getItem(),
                booking.getStart(),
                booking.getEnd(),
                booking.getBooker(),
                booking.getStatus()
        );
    }

    public static Booking fromCreateBookingDto(CreateBookingDto booking, User booker, Item item, BookingStatus status) {
        return new Booking(
                booking.getId(),
                item,
                booker,
                booking.getStart(),
                booking.getEnd(),
                status
        );
    }

    public static ConfirmedBookingDto toConfirmedBookingDto(Booking booking) {
        return new ConfirmedBookingDto(
                booking.getId(),
                booking.getStatus(),
                booking.getBooker(),
                booking.getItem(),
                booking.getStart(),
                booking.getEnd()
        );
    }

    public static BookingItemDto toBookingItemDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingItemDto(booking.getId(), booking.getBooker().getId());
    }
}
