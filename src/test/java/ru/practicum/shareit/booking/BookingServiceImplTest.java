package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ConfirmedBookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    BookingService bookingService;

    User user = new User(1L, "Ivan", "ivan@mail.com");
    User secondUser = new User(2L, "Alex", "alex@mail.com");
    Item item = new Item(1L, "test", "description", secondUser, true, null);

    CreateBookingDto createBookingDto = new CreateBookingDto(1L, 1L,
            LocalDateTime.of(2022, 12, 12, 13, 0),
            LocalDateTime.of(2022, 12, 13, 13, 0));

    ConfirmedBookingDto confirmedBookingDto = new ConfirmedBookingDto(1L,
            BookingStatus.APPROVED,
            user,
            item,
            LocalDateTime.of(2022, 12, 12, 13, 0),
            LocalDateTime.of(2022, 12, 13, 13, 0));

    BookingDto bookingDto = new BookingDto(1L, item,
            LocalDateTime.of(2022, 12, 12, 13, 0),
            LocalDateTime.of(2022, 12, 13, 13, 0),
            user,
            BookingStatus.WAITING);

    List<BookingDto> bookingDtoList = List.of(bookingDto);

    @Test
    void addBooking() {
        Mockito.when(bookingService.addBooking(createBookingDto, 1L))
                .thenReturn(bookingDto);
        BookingDto bookingDto1 = bookingService.addBooking(createBookingDto, 1L);

        assertEquals(bookingDto, bookingDto1);
        Mockito.verify(bookingService, Mockito.times(1)).addBooking(createBookingDto, 1L);
    }

    @Test
    void confirmBooking() {
        Mockito.when(bookingService.confirmBooking(1L, 2L, true))
                .thenReturn(confirmedBookingDto);
        ConfirmedBookingDto confirmedBooking = bookingService.confirmBooking(1L, 2L, true);

        assertEquals(confirmedBookingDto, confirmedBooking);
        Mockito.verify(bookingService, Mockito.times(1))
                .confirmBooking(1L, 2L, true);
    }

    @Test
    void getBooking() {
        Mockito.when(bookingService.getBooking(1L, 1L))
                .thenReturn(bookingDto);
        BookingDto booking = bookingService.getBooking(1L, 1L);

        assertEquals(bookingDto, booking);
        Mockito.verify(bookingService, Mockito.times(1))
                .getBooking(1L, 1L);
    }

    @Test
    void getAllBookings() {
        Mockito.when(bookingService.getAllBookings("ALL", 2L, true, 0, 10))
                .thenReturn(bookingDtoList);
        List<BookingDto> bookings = bookingService.getAllBookings("ALL", 2L, true, 0, 10);
        assertEquals(1, bookings.size());
        assertEquals(bookingDtoList.get(0), bookings.get(0));
    }
}
