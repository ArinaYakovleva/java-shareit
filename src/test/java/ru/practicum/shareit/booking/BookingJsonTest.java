package ru.practicum.shareit.booking;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.ConfirmedBookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@JsonTest
class BookingJsonTest {
    @Autowired
    private JacksonTester<BookingDto> bookingDtoJson;

    @Autowired
    private JacksonTester<BookingItemDto> bookingItemDtoJson;

    @Autowired
    private JacksonTester<ConfirmedBookingDto> confirmedBookingDtoJson;

    @Autowired
    private JacksonTester<CreateBookingDto> createBookingDtoJson;

    User user = new User(1L, "Alex", "alex@mail.com");

    Item item = new Item(1L, "test", "description", user, true, null);

    @Test
    void testBookingDto() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, item,
                LocalDateTime.of(2022, 12, 10, 15, 0),
                LocalDateTime.of(2022, 12, 11, 15, 0),
                user,
                BookingStatus.WAITING);

        JsonContent<BookingDto> result = bookingDtoJson.write(bookingDto);

        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2022-12-10T15:00:00");
        Assertions.assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2022-12-11T15:00:00");
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Alex");
        Assertions.assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }

    @Test
    void testBookingItemDto() throws Exception {
        BookingItemDto bookingItemDto = new BookingItemDto(1L, 1L);

        JsonContent<BookingItemDto> result = bookingItemDtoJson.write(bookingItemDto);
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(1);
    }

    @Test
    void testConfirmedBookingDto() throws Exception {
        ConfirmedBookingDto confirmedBookingDto = new ConfirmedBookingDto(1L, BookingStatus.APPROVED, user, item);

        JsonContent<ConfirmedBookingDto> result = confirmedBookingDtoJson.write(confirmedBookingDto);

        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Alex");
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("test");
    }

    @Test
    void testCreateBookingDtoJson() throws Exception {
        CreateBookingDto createBookingDto = new CreateBookingDto(1L, 1L,
                LocalDateTime.of(2022, 12, 10, 15, 0),
                LocalDateTime.of(2022, 12, 11, 15, 0));

        JsonContent<CreateBookingDto> result = createBookingDtoJson.write(createBookingDto);

        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2022-12-10T15:00:00");
        Assertions.assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2022-12-11T15:00:00");
    }
}
