package ru.practicum.shareit.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExceptionHandlerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final UserDto user = new UserDto(1L, "test", "test@mail.com");

    private final ItemDto itemDto = new ItemDto(1L, "test", "test", true, null);
    @Test
    void handleNotFoundException() throws Exception {
        mockMvc.perform(get("/users/1000")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void handleNotValidException() throws Exception {
        UserDto userDto = new UserDto(null, null, "test@mail.ru");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleForbiddenException() throws Exception {
        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(itemDto))
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(delete("/items/1")
                .header("X-Sharer-User-Id", 2)
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());
    }

    @Test
    void handleMissingHeader() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleSqlException() throws Exception {
        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        UserDto newUser = new UserDto(2L, "name", "test@mail.com");

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(newUser))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void handleBadRequestException() throws Exception {
        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        ItemDto notAvailableItem = new ItemDto(1L, "test", "test", false, null);

        mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(notAvailableItem))
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

        CreateBookingDto createBookingDto = new CreateBookingDto(1L, 1L,
                LocalDateTime.of(2022, 12, 12, 12, 0),
                LocalDateTime.of(2022, 12, 12, 12, 0)
        );

        mockMvc.perform(post("/bookings")
                .content(objectMapper.writeValueAsString(createBookingDto))
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON));

    }
}
