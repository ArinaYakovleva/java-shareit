package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDTOMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDTOMapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BookingRepository bookingRepository;
    UserDto userDto;
    UserDto secondUser;
    ItemDto firstItem;
    ItemDto secondItem;
    ItemBookingDto itemBookingDto;

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
        secondItem = new ItemDto(2L, "дрель", "отличная дрель", false, null);

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

        // Создание прошедшего бронирования
        LocalDateTime startLast = LocalDateTime.of(2022, 10, 20, 14, 30);
        LocalDateTime endLast = LocalDateTime.of(2022, 10, 23, 20, 0);
        Booking booking = new Booking(1L,
                ItemDTOMapper.fromItemDto(firstItem, UserDTOMapper.fromUserDto(userDto), null),
                UserDTOMapper.fromUserDto(secondUser),
                startLast,
                endLast,
                BookingStatus.APPROVED);
        bookingRepository.save(booking);

        mockMvc.perform(patch("/bookings/1?approved=true")
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        CommentDto comment = new CommentDto(1L, "отличная отвертка", "Ivan",
                LocalDateTime.of(2022, 10, 24, 16, 0));

        BookingItemDto bookingItemDto = new BookingItemDto(1L, 2L);
        mockMvc.perform(post("/items/1/comment")
                .content(objectMapper.writeValueAsString(comment))
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        itemBookingDto = new ItemBookingDto(1L,
                "отвертка",
                "хорошая отвертка",
                true,
                bookingItemDto,
                null,
                List.of(comment));
    }

    @Test
    void getItem() throws Exception {
        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemBookingDto.getName())))
                .andExpect(jsonPath("$.description", is(itemBookingDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemBookingDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.lastBooking.id", is(itemBookingDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking", nullValue()))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id", is(1L), Long.class));
    }

    @Test
    void getNotFoundItem() throws Exception {
        mockMvc.perform(get("/items/3")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllItems() throws Exception {
        mockMvc.perform(get("/items?from=0&to=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }

    @Test
    void searchItems() throws Exception {
        mockMvc.perform(get("/items/search?text=отВеРт&from=0&to=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }

    @Test
    void searchItemsNotFound() throws Exception {
        mockMvc.perform(get("/items/search?text=дрель&from=0&to=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addItem() throws Exception {
        ItemDto itemDto = new ItemDto(3L,
                "газонокосилка",
                "kercher газонокосилка",
                true,
                null);

        mockMvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3L), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", nullValue()));

        mockMvc.perform(get("/items?from=0&to=10")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void addItemWithoutName() throws Exception {
        ItemDto itemDto = new ItemDto(3L,
                null,
                "kercher газонокосилка",
                true,
                null);
        mockMvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemWithoutHeader() throws Exception {
        ItemDto itemDto = new ItemDto(3L,
                "test",
                "test description",
                true,
                null);
        mockMvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemWithoutDescription() throws Exception {
        ItemDto itemDto = new ItemDto(3L,
                "name",
                null,
                true,
                null);
        mockMvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemWithoutAvailable() throws Exception {
        ItemDto itemDto = new ItemDto(3L,
                "name",
                "description",
                null,
                null);
        mockMvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemWrongUser() throws Exception {
        ItemDto itemDto = new ItemDto(3L,
                "test name",
                "test description",
                true,
                null);
        mockMvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 10)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItemUnknownRequest() throws Exception {
        ItemDto itemDto = new ItemDto(3L,
                "test name",
                "test description",
                true,
                1L);
        mockMvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment() throws Exception {
        CommentDto comment = new CommentDto(2L, "супер отвертка", "Ivan",
                LocalDateTime.of(2022, 10, 24, 16, 0));
        mockMvc.perform(post("/items/1/comment")
                        .content(objectMapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(2)));

    }

    @Test
    void addCommentWithNoBookings() throws Exception {
        CommentDto comment = new CommentDto(2L, "супер отвертка", "Ivan",
                LocalDateTime.of(2022, 10, 24, 16, 0));
        mockMvc.perform(post("/items/2/comment")
                        .content(objectMapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCommentWithNotFoundItem() throws Exception {
        CommentDto comment = new CommentDto(2L, "супер отвертка", "Ivan",
                LocalDateTime.of(2022, 10, 24, 16, 0));
        mockMvc.perform(post("/items/3/comment")
                        .content(objectMapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addCommentWrongUser() throws Exception {
        CommentDto comment = new CommentDto(2L, "test", "test",
                LocalDateTime.of(2022, 10, 24, 16, 0));
        mockMvc.perform(post("/items/1/comment")
                        .content(objectMapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", 10)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void editItem() throws Exception {
        ItemDto itemDto = new ItemDto(2L,
                "name",
                "description",
                false,
                null);
        mockMvc.perform(patch("/items/2")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2L), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", nullValue()));
    }

    @Test
    void editNotFoundItem() throws Exception {
        ItemDto itemDto = new ItemDto(2L,
                "name",
                "description",
                false,
                null);
        mockMvc.perform(patch("/items/3")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void editByWrongUser() throws Exception {
        ItemDto itemDto = new ItemDto(2L,
                "name",
                "description",
                false,
                null);
        mockMvc.perform(patch("/items/2")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteItem() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteNotFoundItem() throws Exception {
        mockMvc.perform(delete("/items/3")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteForbiddenItem() throws Exception {
        mockMvc.perform(delete("/items/2")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
