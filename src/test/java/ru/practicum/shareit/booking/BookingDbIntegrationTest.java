package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDTOMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDTOMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingDbIntegrationTest {
    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    User user;

    User secondUser;
    Item firstItem;

    Item secondItem;

    Booking firstItemPastBooking;
    Booking firstItemFutureBooking;

    Booking secondItemFutureBooking;

    @BeforeEach
    void init() {
        UserDto userDto = new UserDto(1L, "test user", "test@mail.ru");
        this.user = userRepository.saveAndFlush(UserDTOMapper.fromUserDto(userDto));

        ItemDto firstItem = new ItemDto(1L, "отвертка", "хорошая отвертка", true, null);
        this.firstItem = itemRepository.saveAndFlush(ItemDTOMapper.fromItemDto(firstItem, user, null));

        UserDto userDto1 = new UserDto(2L, "Ivan", "ivan@mail.ru");
        this.secondUser = userRepository.saveAndFlush(UserDTOMapper.fromUserDto(userDto1));

        ItemDto secondItemDto = new ItemDto(2L, "дрель", "отличная дрель", false, null);
        this.secondItem = itemRepository.save(ItemDTOMapper.fromItemDto(secondItemDto, secondUser, null));

        this.firstItemPastBooking = bookingRepository.saveAndFlush(
                new Booking(1L, this.firstItem, secondUser,
                        LocalDateTime.of(2021, 5, 29, 18, 0),
                        LocalDateTime.of(2021, 5, 30, 18, 0),
                        BookingStatus.APPROVED)
        );

        this.firstItemFutureBooking = bookingRepository.saveAndFlush(
                new Booking(2L, this.firstItem, secondUser,
                        LocalDateTime.of(2022, 12, 29, 18, 0),
                        LocalDateTime.of(2022, 12, 30, 18, 0),
                        BookingStatus.WAITING)
        );

        this.secondItemFutureBooking = bookingRepository.saveAndFlush(
                new Booking(3L, this.secondItem, user,
                        LocalDateTime.of(2023, 1, 1, 18, 0),
                        LocalDateTime.of(2023, 1, 3, 18, 0),
                        BookingStatus.WAITING)
        );
    }

    @Test
    void addBooking() {
        Booking bookingToAdd = new Booking(4L, this.firstItem, secondUser,
                LocalDateTime.of(2023, 1, 29, 18, 0),
                LocalDateTime.of(2023, 1, 30, 18, 0),
                BookingStatus.WAITING);
        Booking createdBooking = bookingRepository.saveAndFlush(bookingToAdd);

        List<Booking> bookingList = bookingRepository.findAll();

        assertEquals(bookingToAdd, createdBooking);
        assertEquals(4, bookingList.size());
    }

    @Test
    void confirmBooking() {
        Booking bookingToConfirm = new Booking(3L, this.secondItem, user,
                LocalDateTime.of(2023, 1, 1, 18, 0),
                LocalDateTime.of(2023, 1, 3, 18, 0),
                BookingStatus.REJECTED);

        Booking confirmedBooking = bookingRepository.saveAndFlush(bookingToConfirm);

        Booking foundBooking = bookingRepository.findById(3L).get();

        assertEquals(bookingToConfirm, confirmedBooking);
        assertEquals(BookingStatus.REJECTED, foundBooking.getStatus());
    }

    @Test
    void getBooking() {
        Booking foundBooking = bookingRepository.findById(1L).get();
        Optional<Booking> notFoundBooking = bookingRepository.findById(10L);

        assertEquals(firstItemPastBooking, foundBooking);
        assertFalse(notFoundBooking.isPresent());
    }

    @Test
    void getAllBookingsByOwner() {
        CustomPageRequest pageable = new CustomPageRequest(0, 10);
        List<Booking> bookings = bookingRepository.findAllByItem_Owner_IdOrderByEndDesc(1L, pageable);
        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0), firstItemFutureBooking);
        assertEquals(bookings.get(1), firstItemPastBooking);
    }

    @Test
    void getAllBookingsByBooker() {
        CustomPageRequest pageable = new CustomPageRequest(0, 10);
        List<Booking> bookings = bookingRepository.findAllByBooker_IdOrderByEndDesc(1L, pageable);
        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), secondItemFutureBooking);
    }

}
