package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {
    private final User user = new User(1L, "name", "name@mail.com");

    private final LocalDateTime start = LocalDateTime.of(2022, 12, 12, 11, 0);

    private final LocalDateTime end = LocalDateTime.of(2022, 12, 13, 11, 0);

    private final Item item = new Item(1L, "test", "test", user, true, null);

    private final Booking firstBooking = new Booking(1L, item, user, start, end, BookingStatus.APPROVED);

    private final Booking secondBooking = new Booking(1L, item, user, start, end, BookingStatus.APPROVED);

    @Test
    void testEquals() {
        assertEquals(firstBooking, secondBooking);
    }

    @Test
    void testHashCode() {
        assertEquals(firstBooking.hashCode(), secondBooking.hashCode());
    }
}
