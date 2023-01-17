package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ConfirmedBookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private final User user = new User(1L, "Ivan", "ivan@mail.com");
    private final User secondUser = new User(2L, "Alex", "alex@mail.com");

    private final Item item = new Item(1L, "test", "description",
            secondUser, true, null);

    private final LocalDateTime start = LocalDateTime.of(2022, 12, 12, 13, 0);

    private final LocalDateTime end = LocalDateTime.of(2022, 12, 13, 13, 0);
    private final CreateBookingDto createBookingDto = new CreateBookingDto(
            1L,
            1L,
            start,
            end
    );

    private final BookingDto bookingDto = new BookingDto(1L, item,
            start,
            end,
            user,
            BookingStatus.WAITING);

    private final ConfirmedBookingDto confirmedBookingDto = new ConfirmedBookingDto(1L,
            BookingStatus.APPROVED,
            user,
            item,
            start,
            end);

    @Test
    void addBooking() throws Exception {
        Mockito.when(bookingService.addBooking(createBookingDto, 1L))
                .thenReturn(bookingDto);
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(1)));
    }

    @Test
    void confirmBooking() throws Exception {
        Mockito.when(bookingService.confirmBooking(1L, 2L, true))
                .thenReturn(confirmedBookingDto);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.booker.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(1)));
    }

    @Test
    void getBooking() throws Exception {
        Mockito.when(bookingService.getBooking(1L, 2L))
                .thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.booker.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(1)));
    }

    @Test
    void getAllBookings() throws Exception {
        Mockito.when(bookingService.getAllBookings("ALL", 1L, false, 0, 10))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings?state=ALL&from=0&to=10")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("WAITING")))
                .andExpect(jsonPath("$[0].booker.id", is(1)))
                .andExpect(jsonPath("$[0].item.id", is(1)));
    }

    @Test
    void getBookingsByOwner() throws Exception {
        Mockito.when(bookingService.getAllBookings("ALL", 2L, true, 0, 10))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner?state=ALL&from=0&to=10")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("WAITING")))
                .andExpect(jsonPath("$[0].booker.id", is(1)))
                .andExpect(jsonPath("$[0].item.id", is(1)));
    }
}
