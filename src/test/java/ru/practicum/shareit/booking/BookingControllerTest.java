package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDTOMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDTOMapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ObjectMapper objectMapper;

    UserDto userDto;

    UserDto secondUser;
    ItemDto firstItem;
    ItemDto secondItem;
    Booking pastBooking;
    CreateBookingDto bookingDto;

    @BeforeEach
    void init() throws Exception {
        userDto = new UserDto(1L, "test user", "test@mail.ru");
        secondUser = new UserDto(2L, "Ivan", "ivan@mail.ru");

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(secondUser))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        firstItem = new ItemDto(1L, "отвертка", "хорошая отвертка", true, null);
        secondItem = new ItemDto(2L, "дрель", "отличная дрель", true, null);
        ItemDto unavailableItem = new ItemDto(3L, "велосипед", "велосипед stells", false, null);

        mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(firstItem))
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(secondItem))
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(unavailableItem))
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        LocalDateTime startLast = LocalDateTime.of(2022, 10, 20, 14, 30);
        LocalDateTime endLast = LocalDateTime.of(2022, 10, 23, 20, 0);

        pastBooking = new Booking(1L,
                ItemDTOMapper.fromItemDto(firstItem, UserDTOMapper.fromUserDto(userDto), null),
                UserDTOMapper.fromUserDto(secondUser),
                startLast,
                endLast,
                BookingStatus.APPROVED);

        bookingRepository.save(pastBooking);

        LocalDateTime futureBookingStart = LocalDateTime.of(2022, 12, 20, 14, 30);
        LocalDateTime futureBookingEnd = LocalDateTime.of(2022, 12, 21, 14, 30);
        bookingDto = new CreateBookingDto(2L, 1L, futureBookingStart, futureBookingEnd);

        mockMvc.perform(post("/bookings")
                .content(objectMapper.writeValueAsString(bookingDto))
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        LocalDateTime secondBookingStart = LocalDateTime.of(2022, 12, 22, 14, 30);
        LocalDateTime secondBookingEnd = LocalDateTime.of(2022, 12, 23, 14, 30);

        CreateBookingDto secondBookingDto = new CreateBookingDto(3L, 2L, secondBookingStart, secondBookingEnd);

        mockMvc.perform(post("/bookings")
                .content(objectMapper.writeValueAsString(secondBookingDto))
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(patch("/bookings/3?approved=true")
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void addBooking() throws Exception {
        LocalDateTime start = LocalDateTime.of(2022, 12, 27, 14, 30);
        LocalDateTime end = LocalDateTime.of(2022, 12, 28, 14, 30);

        CreateBookingDto createBookingDto = new CreateBookingDto(4L, 2L, start, end);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.itemId", is(createBookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.start", is("2022-12-27T14:30:00")))
                .andExpect(jsonPath("$.end", is("2022-12-28T14:30:00")));
    }

    @Test
    void addBookingWrongUser() throws Exception {
        LocalDateTime start = LocalDateTime.of(2022, 12, 30, 14, 30);
        LocalDateTime end = LocalDateTime.of(2023, 1, 1, 14, 30);

        CreateBookingDto createBookingDto = new CreateBookingDto(5L, 2L, start, end);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .header("X-Sharer-User-Id", 3)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookingWrongItem() throws Exception {
        LocalDateTime start = LocalDateTime.of(2022, 12, 30, 14, 30);
        LocalDateTime end = LocalDateTime.of(2023, 1, 1, 14, 30);

        CreateBookingDto createBookingDto = new CreateBookingDto(5L, 5L, start, end);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookingWrongDate() throws Exception {
        LocalDateTime start = LocalDateTime.of(2022, 12, 30, 14, 30);
        LocalDateTime end = LocalDateTime.of(2022, 12, 29, 14, 30);

        CreateBookingDto createBookingDto = new CreateBookingDto(4L, 2L, start, end);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBookingPastDate() throws Exception {
        LocalDateTime start = LocalDateTime.of(2022, 10, 30, 14, 30);
        LocalDateTime end = LocalDateTime.of(2022, 10, 30, 15, 30);

        CreateBookingDto createBookingDto = new CreateBookingDto(4L, 2L, start, end);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBookingOverlappedDates() throws Exception {
        LocalDateTime start = LocalDateTime.of(2022, 12, 22, 14, 30);
        LocalDateTime end = LocalDateTime.of(2022, 12, 23, 14, 30);

        CreateBookingDto createBookingDto = new CreateBookingDto(4L, 2L, start, end);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBookingToUnavailableItem() throws Exception {
        LocalDateTime start = LocalDateTime.of(2022, 12, 30, 14, 30);
        LocalDateTime end = LocalDateTime.of(2022, 12, 30, 15, 30);

        CreateBookingDto createBookingDto = new CreateBookingDto(4L, 3L, start, end);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBookingByOwner() throws Exception {
        LocalDateTime start = LocalDateTime.of(2022, 12, 30, 14, 30);
        LocalDateTime end = LocalDateTime.of(2022, 12, 30, 15, 30);

        CreateBookingDto createBookingDto = new CreateBookingDto(4L, 2L, start, end);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmBooking() throws Exception {
        mockMvc.perform(patch("/bookings/2?approved=true")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2L), Long.class))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void confirmWrongBooking() throws Exception {
        mockMvc.perform(patch("/bookings/5?approved=true")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmBookingWrongUser() throws Exception {
        mockMvc.perform(patch("/bookings/2?approved=true")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmApprovedBooking() throws Exception {
        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking() throws Exception {
        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pastBooking.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(pastBooking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.start", is("2022-10-20T14:30:00")))
                .andExpect(jsonPath("$.end", is("2022-10-23T20:00:00")))
                .andExpect(jsonPath("$.booker.id", is(pastBooking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void getWrongBooking() throws Exception {
        mockMvc.perform(get("/bookings/5")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingWrongUser() throws Exception {
        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 3)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBookings() throws Exception {
        // BY BOOKER
        mockMvc.perform(get("/bookings/?from=0&size=10")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[1].id", is(1)));
        // BY OWNER
        mockMvc.perform(get("/bookings/owner?from=0&size=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[1].id", is(1)));
    }

    @Test
    void getCurrentBookings() throws Exception {
        // BY BOOKER
        mockMvc.perform(get("/bookings/?state=CURRENT&from=0&size=10")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // BY OWNER
        mockMvc.perform(get("/bookings/owner?state=CURRENT&from=0&size=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getFutureBookingsByOwner() throws Exception {
        // BY BOOKER
        mockMvc.perform(get("/bookings/?state=FUTURE&from=0&size=10")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].start", is("2022-12-20T14:30:00")))
                .andExpect(jsonPath("$[0].end", is("2022-12-21T14:30:00")));

        // BY OWNER
        mockMvc.perform(get("/bookings/owner?state=FUTURE&from=0&size=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].start", is("2022-12-20T14:30:00")))
                .andExpect(jsonPath("$[0].end", is("2022-12-21T14:30:00")));

    }

    @Test
    void getPastBookingsByOwner() throws Exception {
        // BY BOOKER
        mockMvc.perform(get("/bookings/?state=PAST&from=0&size=10")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(pastBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(pastBooking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is("2022-10-20T14:30:00")))
                .andExpect(jsonPath("$[0].end", is("2022-10-23T20:00:00")));

        // BY OWNER
        mockMvc.perform(get("/bookings/owner?state=PAST&from=0&size=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(pastBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(pastBooking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is("2022-10-20T14:30:00")))
                .andExpect(jsonPath("$[0].end", is("2022-10-23T20:00:00")));
    }

    @Test
    void getRejectedBookingsByOwner() throws Exception {
        LocalDateTime start = LocalDateTime.of(2022, 12, 23, 12, 0);
        LocalDateTime end = LocalDateTime.of(2022, 12, 24, 12, 0);
        CreateBookingDto createBookingDto = new CreateBookingDto(4L, 1L, start, end);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/bookings/4?approved=false")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings/owner?state=REJECTED&from=0&size=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(createBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(1)))
                .andExpect(jsonPath("$[0].start", is("2022-12-23T12:00:00")))
                .andExpect(jsonPath("$[0].end", is("2022-12-24T12:00:00")));

        //BY BOOKER
        mockMvc.perform(get("/bookings?state=REJECTED&from=0&size=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getWaitingBookingsByOwner() throws Exception {
        mockMvc.perform(get("/bookings/?state=WAITING&from=0&size=10")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(1)))
                .andExpect(jsonPath("$[0].start", is("2022-12-20T14:30:00")))
                .andExpect(jsonPath("$[0].end", is("2022-12-21T14:30:00")));

        mockMvc.perform(get("/bookings/owner?state=WAITING&from=0&size=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(1)))
                .andExpect(jsonPath("$[0].start", is("2022-12-20T14:30:00")))
                .andExpect(jsonPath("$[0].end", is("2022-12-21T14:30:00")));
    }

    @Test
    void getBookingsWrongState() throws Exception {
        mockMvc.perform(get("/bookings/?state=WAITED&from=0&size=10")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsWrongUser() throws Exception {
        mockMvc.perform(get("/bookings/?state=WAITING&from=0&size=10")
                        .header("X-Sharer-User-Id", 3)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
