package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ConfirmedBookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public CreateBookingDto addBooking(@RequestBody CreateBookingDto bookingDto, @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        return bookingService.addBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public ConfirmedBookingDto confirmBooking(@PathVariable Long bookingId,
                                              @RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam Boolean approved) {
        return bookingService.confirmBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllBookings(@RequestParam(required = false, defaultValue = "ALL") String state, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getAllBookings(state, userId, false);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(@RequestParam(required = false, defaultValue = "ALL") String state, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getAllBookings(state, userId, true);
    }
}
