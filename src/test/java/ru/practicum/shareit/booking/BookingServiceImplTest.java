package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ConfirmedBookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.exception.exceptions.BadRequestException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    private final User user = new User(1L, "Ivan", "ivan@mail.com");
    private final User secondUser = new User(2L, "Alex", "alex@mail.com");
    private final Item item = new Item(1L, "test", "description",
            secondUser, true, null);

    private final Item unavailableItem = new Item(1L, "test", "description",
            secondUser, false, null);

    private final CreateBookingDto createBookingDto = new CreateBookingDto(1L, 1L,
            LocalDateTime.of(2022, 12, 12, 13, 0),
            LocalDateTime.of(2022, 12, 13, 13, 0));

    private final ConfirmedBookingDto confirmedBookingDto = new ConfirmedBookingDto(1L,
            BookingStatus.APPROVED,
            user,
            item,
            LocalDateTime.of(2022, 12, 12, 13, 0),
            LocalDateTime.of(2022, 12, 13, 13, 0));

    private final Booking booking = new Booking(1L, item, user,
            LocalDateTime.of(2022, 12, 12, 13, 0),
            LocalDateTime.of(2022, 12, 13, 13, 0),
            BookingStatus.WAITING);

    private final Booking approvedBooking = new Booking(1L, item, user,
            LocalDateTime.of(2022, 12, 12, 13, 0),
            LocalDateTime.of(2022, 12, 13, 13, 0),
            BookingStatus.APPROVED);

    private final BookingDto bookingDto = new BookingDto(1L, item,
            LocalDateTime.of(2022, 12, 12, 13, 0),
            LocalDateTime.of(2022, 12, 13, 13, 0),
            user,
            BookingStatus.WAITING);

    private final Booking currentBooking = new Booking(1L, item, user,
            LocalDateTime.of(2022, 11, 26, 13, 0),
            LocalDateTime.of(2022, 12, 1, 13, 0),
            BookingStatus.APPROVED);

    private final BookingDto currentBookingDto = new BookingDto(1L, item,
            LocalDateTime.of(2022, 11, 26, 13, 0),
            LocalDateTime.of(2022, 12, 1, 13, 0),
            user,
            BookingStatus.APPROVED);

    private final Booking pastBooking = new Booking(1L, item, user,
            LocalDateTime.of(2022, 10, 26, 13, 0),
            LocalDateTime.of(2022, 10, 27, 13, 0),
            BookingStatus.APPROVED);

    private final BookingDto pastBookingDto = new BookingDto(1L, item,
            LocalDateTime.of(2022, 10, 26, 13, 0),
            LocalDateTime.of(2022, 10, 27, 13, 0),
            user,
            BookingStatus.APPROVED);

    private final Booking futureBooking = new Booking(1L, item, user,
            LocalDateTime.of(2022, 12, 26, 13, 0),
            LocalDateTime.of(2022, 12, 27, 13, 0),
            BookingStatus.APPROVED);

    private final BookingDto futureBookingDto = new BookingDto(1L, item,
            LocalDateTime.of(2022, 12, 26, 13, 0),
            LocalDateTime.of(2022, 12, 27, 13, 0),
            user,
            BookingStatus.APPROVED);

    private final Booking rejectedBooking = new Booking(1L, item, user,
            LocalDateTime.of(2022, 12, 26, 13, 0),
            LocalDateTime.of(2022, 12, 27, 13, 0),
            BookingStatus.REJECTED);

    private final BookingDto rejectedBookingDto = new BookingDto(1L, item,
            LocalDateTime.of(2022, 12, 26, 13, 0),
            LocalDateTime.of(2022, 12, 27, 13, 0),
            user,
            BookingStatus.REJECTED);

    @Test
    void addBooking() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.saveAndFlush(booking))
                .thenReturn(booking);

        BookingDto createdBooking = bookingService.addBooking(createBookingDto, 1L);

        assertEquals(bookingDto, createdBooking);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .saveAndFlush(booking);
    }

    @Test
    void addBookingWrongUser() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            bookingService.addBooking(createBookingDto, 2L);
        });

        assertEquals("Пользователь с id=2 не найден", notFoundException.getMessage());
    }

    @Test
    void addBookingWrongItem() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.empty());
        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            bookingService.addBooking(createBookingDto, 1L);
        });

        assertEquals("Вещь с id=1 не найдена", notFoundException.getMessage());
    }

    @Test
    void addBookingWrongDate() {
        LocalDateTime start = LocalDateTime.of(2022, 12, 30, 14, 30);
        LocalDateTime end = LocalDateTime.of(2022, 12, 29, 14, 30);
        CreateBookingDto createBookingDto = new CreateBookingDto(null, 2L, start, end);


        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            bookingService.addBooking(createBookingDto, 1L);
        });

        assertEquals("Дата окончания бронирования не может быть раньше начала", badRequestException.getMessage());
    }

    @Test
    void addBookingPastDate() {
        LocalDateTime start = LocalDateTime.of(2022, 10, 30, 14, 30);
        LocalDateTime end = LocalDateTime.of(2022, 10, 30, 15, 30);

        CreateBookingDto createBookingDto = new CreateBookingDto(null, 2L, start, end);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            bookingService.addBooking(createBookingDto, 1L);
        });

        assertEquals("Дата начала бронирования не может быть в прошлом", badRequestException.getMessage());
    }

    @Test
    void addBookingUnavailableDate() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllOverlappedBookings(1L, createBookingDto.getStart(),
                createBookingDto.getEnd()))
                .thenReturn(List.of(booking));

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            bookingService.addBooking(createBookingDto, 1L);
        });

        assertEquals("Вещь недоступна для бронирования в эти даты", badRequestException.getMessage());
    }

    @Test
    void addBookingUnavailableItem() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(unavailableItem));
        Mockito.when(bookingRepository.findAllOverlappedBookings(1L, createBookingDto.getStart(),
                        createBookingDto.getEnd()))
                .thenReturn(List.of());

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            bookingService.addBooking(createBookingDto, 1L);
        });

        assertEquals("Вещь с id=1 не доступна для бронирования", badRequestException.getMessage());
    }

    @Test
    void addBookingByOwner() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(secondUser));
        Mockito.when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllOverlappedBookings(1L, createBookingDto.getStart(),
                        createBookingDto.getEnd()))
                .thenReturn(List.of());

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            bookingService.addBooking(createBookingDto, 2L);
        });

        assertEquals("Владелец вещи не может бронировать свою вещь", notFoundException.getMessage());
    }
    @Test
    void confirmBooking() {
        Mockito.when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

         System.out.println(booking.getStatus());
        Mockito.when(bookingRepository.saveAndFlush(booking))
                .thenReturn(booking);

        ConfirmedBookingDto confirmedBooking = bookingService.confirmBooking(1L, 2L, true);

        assertEquals(confirmedBookingDto, confirmedBooking);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .saveAndFlush(booking);
    }

    @Test
    void confirmBookingNotFoundBooking() {
        Mockito.when(bookingRepository.findById(2L))
                .thenReturn(Optional.empty());
        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            bookingService.confirmBooking(2L, 1L, true);
        });

        assertEquals("Бронирование с id=2 не найдено", notFoundException.getMessage());
    }

    @Test
    void confirmBookingByWrongUser() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            bookingService.confirmBooking(1L, 1L, true);
        });

        assertEquals("Пользователь с id=1 не может подтвердить бронирование", notFoundException.getMessage());
    }

    @Test
    void confirmApprovedBooking() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(approvedBooking));

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            bookingService.confirmBooking(1L, 2L, true);
        });

        assertEquals("Невозможно изменить статус одобренного бронирования с id=1", badRequestException.getMessage());
    }
    @Test
    void getBooking() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));
        BookingDto booking = bookingService.getBooking(1L, 1L);

        assertEquals(bookingDto, booking);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(1L);
    }

    @Test
    void getNotFoundBooking() {
        Mockito.when(bookingRepository.findById(2L))
                .thenReturn(Optional.empty());
        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            bookingService.getBooking(2L, 1L);
        });
        assertEquals("Бронирование с id=2 не найдено", notFoundException.getMessage());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(2L);
    }

    @Test
    void getBookingWrongUser() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            bookingService.getBooking(1L, 10L);
        });
        assertEquals("Пользователь с id=10 не может просматривать это бронирование", notFoundException.getMessage());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(1L);
    }
    @Test
    void getAllBookingsByOwner() {
        CustomPageRequest pageRequest = new CustomPageRequest(0, 10);
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(secondUser));
        Mockito.when(bookingRepository.findAllByItem_Owner_IdOrderByEndDesc(2L, pageRequest))
                .thenReturn(List.of(booking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("ALL", 2L, true, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), bookingDto);
    }

    @Test
    void getAllBookingsByBooker() {
        CustomPageRequest pageRequest = new CustomPageRequest(0, 10);
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByBooker_IdOrderByEndDesc(1L, pageRequest))
                .thenReturn(List.of(booking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("ALL", 1L, false, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), bookingDto);
    }

    @Test
    void getCurrentBookingsByOwner() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(secondUser));
        Mockito.when(bookingRepository.findAllCurrentByOwner(anyLong(), any(), any()))
                .thenReturn(List.of(currentBooking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("CURRENT", 2L, true, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), currentBookingDto);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllCurrentByOwner(anyLong(), any(), any());
    }

    @Test
    void getCurrentBookingsByBooker() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllCurrentByBooker(anyLong(), any(), any()))
                .thenReturn(List.of(currentBooking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("CURRENT", 1L, false, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), currentBookingDto);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllCurrentByBooker(anyLong(), any(), any());
    }

    @Test
    void getPastBookingsByOwner() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(secondUser));
        Mockito.when(bookingRepository.findAllPastByOwner(anyLong(), any(), any()))
                .thenReturn(List.of(pastBooking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("PAST", 2L, true, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), pastBookingDto);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllPastByOwner(anyLong(), any(), any());
    }

    @Test
    void getPastBookingsByBooker() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllPastByBooker(anyLong(), any(), any()))
                .thenReturn(List.of(pastBooking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("PAST", 1L, false, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), pastBookingDto);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllPastByBooker(anyLong(), any(), any());
    }

    @Test
    void getFutureBookingsByOwner() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(secondUser));
        Mockito.when(bookingRepository.findAllFutureByOwner(anyLong(), any(), any()))
                .thenReturn(List.of(futureBooking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("FUTURE", 2L, true, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), futureBookingDto);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllFutureByOwner(anyLong(), any(), any());
    }

    @Test
    void getFutureBookingsByBooker() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllFutureByBooker(anyLong(), any(), any()))
                .thenReturn(List.of(futureBooking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("FUTURE", 1L, false, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), futureBookingDto);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllFutureByBooker(anyLong(), any(), any());
    }

    @Test
    void getWaitingBookingsByOwner() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(secondUser));
        Mockito.when(bookingRepository.findAllByItem_Owner_IdAndStatusOrderByEndDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("WAITING", 2L, true, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), bookingDto);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItem_Owner_IdAndStatusOrderByEndDesc(anyLong(), any(), any());
    }

    @Test
    void getWaitingBookingsByBooking() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByBooker_IdAndStatusOrderByEnd(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("WAITING", 1L, false, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), bookingDto);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBooker_IdAndStatusOrderByEnd(anyLong(), any(), any());
    }

    @Test
    void getRejectedBookingsByOwner() {
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(secondUser));
        Mockito.when(bookingRepository.findAllByItem_Owner_IdAndStatusOrderByEndDesc(anyLong(), any(), any()))
                .thenReturn(List.of(rejectedBooking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("REJECTED", 2L, true, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), rejectedBookingDto);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItem_Owner_IdAndStatusOrderByEndDesc(anyLong(), any(), any());
    }

    @Test
    void getRejectedBookingsByBooking() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByBooker_IdAndStatusOrderByEnd(anyLong(), any(), any()))
                .thenReturn(List.of(rejectedBooking));

        List<BookingDto> bookingDtos = bookingService
                .getAllBookings("REJECTED", 1L, false, 0, 10);
        assertEquals(1, bookingDtos.size());
        assertEquals(bookingDtos.get(0), rejectedBookingDto);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBooker_IdAndStatusOrderByEnd(anyLong(), any(), any());
    }

    @Test
    void getAllBookingsWrongState() {
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            bookingService.getAllBookings("WRONG",1L, true, 1, 10);
        });

        assertEquals("Unknown state: WRONG", badRequestException.getMessage());
    }

    @Test
    void getAllBookingsWrongUser() {
        Mockito.when(userRepository.findById(3L))
                .thenReturn(Optional.empty());
        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            bookingService.getAllBookings("ALL",3L, true, 1, 10);
        });

        assertEquals("Пользователь с id=3 не найден", notFoundException.getMessage());
    }
}
